/*
 * Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.workflow.WorkflowStatus.checkPercent;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.connexience.api.model.EscWorkflowParameterList;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.Pair;
import eu.eubrazilcc.lvl.core.workflow.WorkflowDataObject;
import eu.eubrazilcc.lvl.core.workflow.WorkflowDefinition;
import eu.eubrazilcc.lvl.core.workflow.WorkflowParameters;
import eu.eubrazilcc.lvl.core.workflow.WorkflowStatus;

/**
 * Workflow connector based on e-Science Central.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.esciencecentral.co.uk/">e-Science Central</a>
 * @see <a href="http://sourceforge.net/projects/esciencecentral/">e-Science Central at SourceForge.org</a>
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
			final ImmutableList.Builder<WorkflowDefinition> builder = new ImmutableList.Builder<WorkflowDefinition>();
			for (final EscWorkflow escWorkflow : workflowClient().listAllWorkflows()) {
				builder.add(WorkflowDefinition.builder()
						.id(escWorkflow.getId())
						.name(escWorkflow.getName())
						.description(escWorkflow.getDescription())
						.build());
			}
			return builder.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to list workflows", e);			
		}
	}

	public WorkflowDefinition getWorkflow(final String workflowId) throws IllegalStateException {
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

	public WorkflowParameters getParameters(final String workflowId) {
		checkArgument(isNotBlank(workflowId), "Uninitialized or invalid workflow identifier");
		try {
			final EscWorkflow escWorkflow = workflowClient().getWorkflow(workflowId);
			checkState(escWorkflow != null, "Workflow not found");
			final WorkflowParameters.Builder builder = WorkflowParameters.builder();			
			final Map<String, String> map = workflowClient().listCallableWorkflowParameters(workflowId);
			for (final Map.Entry<String, String> entry : map.entrySet()) {

				// TODO : get default values

				builder.parameter(entry.getKey(), entry.getValue(), "DEFAULT VALUE");
			}
			return builder.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get workflow parameters", e);			
		}
	}

	public String executeWorkflow(final String workflowId, final @Nullable WorkflowParameters parameters) {
		checkArgument(isNotBlank(workflowId), "Uninitialized or invalid workflow identifier");
		final EscWorkflowParameterList parametersList = new EscWorkflowParameterList();
		if (parameters != null) {
			for (final Map.Entry<String, List<Pair<String, String>>> entry : parameters.getParameters().entrySet()) {
				for (final Pair<String, String> pair : entry.getValue()) {
					parametersList.addParameter(entry.getKey(), pair.getKey(), pair.getValue());				
				}
			}
		}
		try {
			final EscWorkflowInvocation invocation = workflowClient().executeWorkflowWithParameters(workflowId, parametersList);
			return invocation.getId();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to execute the workflow", e);
		}
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
					.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to monitor workflow execution", e);
		}
	}

	public String uploadFile(final File inputFile) throws IllegalStateException {
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

	public ImmutableList<WorkflowDataObject> listFiles() {
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
	}

	public void deleteFile(final String documentId) {
		checkArgument(isNotBlank(documentId), "Uninitialized or invalid document identifier");
		try {
			storageClient().deleteDocument(documentId);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to delete file", e);
		}
	}

	public void saveProducts(final String invocationId, final File outputDir) throws IllegalStateException {
		checkArgument(isNotBlank(invocationId), "Uninitialized or invalid invocation identifier");
		try {
			final EscFolder[] folders = storageClient().listChildFolders(invocationId);			
			for (final EscFolder folder : folders) {
				final EscDocument[] docs = storageClient().folderDocuments(folder.getId());
				final File outputDir2 = new File(outputDir, folder.getName());
				if (!outputDir2.canWrite() && !outputDir2.mkdirs()) {
					throw new IllegalStateException("Failed to write on output directory");
				}
				for (final EscDocument doc : docs) {
					final File outputFile = new File(outputDir2, doc.getName());									
					storageClient().download(doc, outputFile);
					LOGGER.trace("Workflow '" + invocationId + "' product saved '" + doc.getId() + "' to local file " + outputFile.getCanonicalPath());
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to retrieve workflow products", e);
		}
	}

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("e-Science Central connector initialized successfully");
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
			LOGGER.info("e-Science Central connector shutdown successfully");
		}
	}

}