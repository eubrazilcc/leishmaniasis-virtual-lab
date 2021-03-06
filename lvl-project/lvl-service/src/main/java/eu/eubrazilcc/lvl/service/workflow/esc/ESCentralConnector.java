/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *   http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package eu.eubrazilcc.lvl.service.workflow.esc;

import static com.connexience.api.model.EscWorkflowInvocation.INVOCATION_FINISHED_WITH_ERRORS;
import static com.connexience.api.model.EscWorkflowInvocation.INVOCATION_STATE_UNKNOWN;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.workflow.WorkflowStatus.checkPercent;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.connexience.api.StorageClient;
import com.connexience.api.WorkflowClient;
import com.connexience.api.model.EscDocument;
import com.connexience.api.model.EscDocumentVersion;
import com.connexience.api.model.EscFolder;
import com.connexience.api.model.EscWorkflow;
import com.connexience.api.model.EscWorkflowInvocation;
import com.connexience.api.model.EscWorkflowParameterDesc;
import com.connexience.api.model.json.JSONObject;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.workflow.WfOption;
import eu.eubrazilcc.lvl.core.workflow.WorkflowDefinition;
import eu.eubrazilcc.lvl.core.workflow.WorkflowParameters;
import eu.eubrazilcc.lvl.core.workflow.WorkflowProduct;
import eu.eubrazilcc.lvl.core.workflow.WorkflowStatus;

/**
 * Workflow connector based on e-Science Central.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.esciencecentral.co.uk/">e-Science Central</a>
 * @see <a href="https://bitbucket.org/digitalinstitute/esciencecentral/">e-Science Central at Bitbucket</a>
 */
public enum ESCentralConnector implements Closeable2 {

	ESCENTRAL_CONN;

	private static final Logger LOGGER = getLogger(ESCentralConnector.class);

	private Lock mutex = new ReentrantLock();
	private WorkflowClient __wfclient = null;
	private StorageClient __stclient = null;

	private WorkflowClient workflowClient() {
		mutex.lock();
		try {
			if (__wfclient == null) {
				__wfclient = new WorkflowClient(CONFIG_MANAGER.getWfHostname(), CONFIG_MANAGER.getWfPort(), CONFIG_MANAGER.isWfSecure(), 
						CONFIG_MANAGER.getWfUsername(), CONFIG_MANAGER.getWfPasswd());
			}
			return __wfclient;
		} finally {
			mutex.unlock();
		}
	}

	private StorageClient storageClient() {
		mutex.lock();
		try {
			if (__stclient == null) {				
				__stclient = new StorageClient(CONFIG_MANAGER.getWfHostname(), CONFIG_MANAGER.getWfPort(), CONFIG_MANAGER.isWfSecure(), 
						CONFIG_MANAGER.getWfUsername(), CONFIG_MANAGER.getWfPasswd());
			}
			return __stclient;
		} finally {
			mutex.unlock();
		}
	}

	public ImmutableList<WorkflowDefinition> listWorkflows() {
		try {
			final ImmutableList.Builder<WorkflowDefinition> builder = new ImmutableList.Builder<>();
			for (final EscWorkflow escWorkflow : workflowClient().listAllWorkflows()) {
				builder.add(WorkflowDefinition.builder()
						.id(escWorkflow.getId())
						.version(escWorkflow.getCurrentVersionNumber())
						.name(escWorkflow.getName())
						.description(escWorkflow.getDescription())
						.build());
			}
			return builder.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to list workflows", e);			
		}
	}

	public WorkflowDefinition getWorkflow(final String workflowId) {
		checkArgument(isNotBlank(workflowId), "Uninitialized or invalid workflow identifier");
		try {
			final EscWorkflow escWorkflow = workflowClient().getWorkflow(workflowId);			
			checkState(escWorkflow != null, "Workflow not found");
			return WorkflowDefinition.builder()
					.id(escWorkflow.getId())
					.name(escWorkflow.getName())
					.description(escWorkflow.getDescription())
					.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get workflow", e);			
		}
	}

	public WorkflowParameters getParameters(final String workflowId, final @Nullable String versionId, final @Nullable List<WfOption> options) {
		checkArgument(isNotBlank(workflowId), "Uninitialized or invalid workflow identifier");
		try {
			// get workflow parameters
			final EscWorkflow escWorkflow = workflowClient().getWorkflow(workflowId);
			checkState(escWorkflow != null, "Workflow not found");
			final WorkflowParameters.Builder builder = WorkflowParameters.builder();
			for (final EscWorkflowParameterDesc desc : workflowClient().listCallableWorkflowParametersEx(workflowId, versionId).values()) {
				builder.parameter(desc.getName(), desc.getValue(), desc.getType(), desc.getDescription(), desc.getOptions());
			}
			final WorkflowParameters parameters = builder.build();
			// parse options and update parameters
			if (options != null) {
				for (final WfOption option : options) {
					Map<String, String> param = null;
					for (int i = 0; i < parameters.getParameters().size() && param == null; i++) {
						if (option.getName().equals(parameters.getParameters().get(i).get("name"))) {
							param = parameters.getParameters().get(i);
						}
					}
					if (param != null) {
						int i = 0;
						// list files
						if (isNotBlank(option.getFolderId())) {
							final Map<String, String> files = ESCENTRAL_CONN.listFiles(option.getFolderId());
							if (files != null) {								
								final Map<String, String> fileOpts = newHashMap();
								for (final Map.Entry<String, String> entry : new TreeMap<>(files).entrySet()) {
									fileOpts.put("option_" + Integer.toString(i), entry.getKey() + "|" + entry.getValue());
									i++;
								}
								param.putAll(fileOpts);
							}
						}
					}
				}
			}
			return parameters;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get workflow parameters", e);			
		}
	}

	public WorkflowParameters getParameters(final String workflowId) {
		return getParameters(workflowId, null, null);
	}

	public String executeWorkflow(final String workflowId, final @Nullable String versionId, final @Nullable WorkflowParameters parameters) {
		checkArgument(isNotBlank(workflowId), "Uninitialized or invalid workflow identifier");
		final JSONObject params = new JSONObject();
		if (parameters != null) {
			for (final Map<String, String> param : parameters.getParameters()) {
				params.put(param.get("name"), param.get("value"));
			}
		}
		try {
			final EscWorkflowInvocation invocation = (versionId == null) ? workflowClient().executeCallableWorkflow(workflowId, versionId, params.toString()):
				workflowClient().executeCallableWorkflow(workflowId, versionId, params.toString());
			return invocation.getId();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to execute the workflow", e);
		}
	}

	public String executeWorkflow(final String workflowId, final @Nullable WorkflowParameters parameters) {
		return executeWorkflow(workflowId, null, parameters);
	}

	public void cancelExecution(final String invocationId) {
		checkArgument(isNotBlank(invocationId), "Uninitialized or invalid invocation identifier");
		try {
			workflowClient().terminateInvocation(invocationId);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to cancel workflow execution", e);
		}
	}

	public WorkflowStatus getStatus(final String invocationId) {
		checkArgument(isNotBlank(invocationId), "Uninitialized or invalid invocation identifier");
		try {
			final EscWorkflowInvocation invocation = workflowClient().getInvocation(invocationId);			
			return WorkflowStatus.builder()
					.completeness(checkPercent(invocation.getPercentComplete()))
					.status(invocation.getStatus())					
					.completed(!invocation.isInProgress())
					.failed(INVOCATION_FINISHED_WITH_ERRORS.equals(invocation.getStatus()) || INVOCATION_STATE_UNKNOWN.equals(invocation.getStatus()))
					.description(invocation.getStatusMessage())					
					.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to monitor workflow execution", e);
		}
	}

	public String uploadFile(final File inputFile) {
		checkArgument(inputFile != null && inputFile.exists(), "Uninitialized or invalid input file");
		try {
			final EscFolder home = storageClient().homeFolder();
			final EscDocumentVersion version = storageClient().upload(home, inputFile);
			final EscDocument uploaded = storageClient().getDocument(version.getDocumentRecordId());
			checkState(uploaded != null, "Uploaded file not found");
			return uploaded.getId();					
		} catch (Exception e) {
			throw new IllegalStateException("Failed to upload file", e);
		}
	}

	public Map<String, String> listFiles(final String folderId) {
		final Map<String, String> map = newHashMap();
		try {
			final EscDocument[] docs = storageClient().folderDocuments(folderId);
			for (final EscDocument doc : docs) {
				map.put(doc.getId(), doc.getName());				
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to list files", e);
		}
		return unmodifiableMap(map);
	}

	/* TODO public ImmutableList<WorkflowDataObject> listFiles() {
		final ImmutableList.Builder<WorkflowDataObject> builder = new ImmutableList.Builder<WorkflowDataObject>();
		try {
			final EscFolder home = storageClient().homeFolder();
			final EscDocument[] docs = storageClient().folderDocuments(home.getId());
			if (docs != null) {
				for (final EscDocument doc : docs) {
					builder.add(WorkflowDataObject.builder()
							.id(doc.getId())
							.name(doc.getName())
							.description(doc.getDescription())
							.created(new Date(doc.getCreationTime()))
							.build());
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to list files", e);
		}
		return builder.build();
	} */

	public void deleteFile(final String documentId) {
		checkArgument(isNotBlank(documentId), "Uninitialized or invalid document identifier");
		try {
			storageClient().deleteDocument(documentId);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to delete file", e);
		}
	}

	public ImmutableList<WorkflowProduct> saveProducts(final String invocationId, final File outputDir) {
		checkArgument(isNotBlank(invocationId), "Uninitialized or invalid invocation identifier");
		final ImmutableList.Builder<WorkflowProduct> builder = new ImmutableList.Builder<>();
		try {
			final EscDocument[] docs = storageClient().folderDocuments(invocationId);
			for (final EscDocument doc : docs) {
				final File outputFile = new File(outputDir, doc.getName());									
				storageClient().download(doc, outputFile);
				builder.add(WorkflowProduct.builder()
						.path(doc.getName())
						.build());
				LOGGER.trace("Workflow '" + invocationId + "' product saved '" + doc.getId() + "' to local file " + outputFile.getCanonicalPath());	
			}

		} catch (Exception e) {
			throw new IllegalStateException("Failed to retrieve workflow products", e);
		}
		return builder.build();
	}

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("e-SC connector initialized successfully");
	}

	@Override
	public void close() throws IOException {
		mutex.lock();
		try {
			if (__wfclient != null) {
				__wfclient = null;
			}
			if (__stclient != null) {
				__stclient = null;
			}
		} finally {
			mutex.unlock();
			LOGGER.info("e-SC connector shutdown successfully");
		}
	}

}