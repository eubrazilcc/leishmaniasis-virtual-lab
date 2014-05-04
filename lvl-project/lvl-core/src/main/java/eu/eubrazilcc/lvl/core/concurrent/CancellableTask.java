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

package eu.eubrazilcc.lvl.core.concurrent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Range.closed;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * Encloses a {@link ListenableFutureTask}, which is a task with cancellation support, adding an identifier
 * that can be used to store and access the task from a resource.
 * @author Erik Torres <ertorser@upv.es>
 * @param <V> - The result type returned by this {@link CancellableTask}'s get methods.
 */
public class CancellableTask<V> {

	public static final Range<Double> PROGRESS_RANGE = closed(0.0d, 100.0d);
	
	private final UUID uuid;
	protected ListenableFutureTask<V> task;
	private double progress = PROGRESS_RANGE.lowerEndpoint();

	public CancellableTask() {
		this.uuid = randomUUID();		
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public @Nullable ListenableFutureTask<V> getTask() {
		return task;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(final double progress) {
		checkArgument(PROGRESS_RANGE.contains(progress), "Progress is not in the valid range: " + PROGRESS_RANGE);
		this.progress = progress;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("uuid", uuid)
				.toString();
	}
	
}