/**
 * Copyright © 2026, m2n GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of the University of Washington nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL UNIVERSITY OF
 * WASHINGTON BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uw.apl.commons.tsk4j.pool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uw.apl.commons.tsk4j.Native;
import edu.uw.apl.commons.tsk4j.base.Closeable;
import edu.uw.apl.commons.tsk4j.base.HeapBuffer;
import edu.uw.apl.commons.tsk4j.filesys.Block;
import edu.uw.apl.commons.tsk4j.filesys.BlockWalk;
import edu.uw.apl.commons.tsk4j.filesys.Directory;
import edu.uw.apl.commons.tsk4j.filesys.DirectoryWalk;
import edu.uw.apl.commons.tsk4j.filesys.File;
import edu.uw.apl.commons.tsk4j.filesys.MetaWalk;
import edu.uw.apl.commons.tsk4j.image.Image;
import edu.uw.apl.commons.tsk4j.volsys.Partition;

/**
 * @author Bernhard Stiftner
 *
 * Java wrapper around the Sleuthkit TSK_POOL_INFO struct and api.
 *
 * Impl note: ANY use of the nativePtr requires that we first check
 * that the pool is not 'closed'.  In other words, any use of
 * nativePtr, and the operation that uses it, is invalid after
 * Pool.close().
 */

public class Pool extends Closeable {

	public static boolean isPool(Image image) {
		return isPool(image, 0L);
	}

	public static boolean isPool(Image image, long sectorOffset) {
		try {
			try (final Pool pool = new Pool(image, sectorOffset)) {
				pool.getType();
				return true;
			}
		} catch (Exception ignored) {
			// ignored
		}
		return false;
	}

	public static boolean isPool(Partition partition) {
		try {
			try (final Pool pool = new Pool(partition)) {
				pool.getType();
				return true;
			}
		} catch (Exception ignored) {
			// ignored
		}
		return false;
	}

	/**
	 * @param ownsImage - if true, then call Image.close on Pool closure
	 */
	public Pool( Image image, boolean ownsImage, long sectorOffset ) throws IOException {
		this.image = image;
		this.ownsImage = ownsImage;
		this.sectorOffset = sectorOffset;
		this.partition = null;
		this.nativePtr = openImage( image.nativePtr(),
							   sectorOffset * image.sectorSize() );
		if( nativePtr == 0 )
			// mimic fls's error message...
			throw new IllegalStateException
				( "Cannot determine pool type" );
	}

	public Pool( Image image, long sectorOffset ) throws IOException {
		this( image, false, sectorOffset );
	}

	public Pool( Image image ) throws IOException {
		this( image, false, 0L );
	}

	public Pool( String path, long sectorOffset ) throws IOException {
		this( new Image( path ), true, sectorOffset );
	}

	public Pool( String path ) throws IOException {
		this( path, 0L );
	}

	public Pool( Partition p ) throws IOException {
		this.image = null;
		this.ownsImage = false;
		this.sectorOffset = -1;
		this.partition = p;
		this.nativePtr = openPartition( p.nativePtr() );
		if( nativePtr == 0 )
			// mimic fls's error message...
			throw new IOException( "Cannot determine pool type" );
	}

	/**
	 * @return Image from which this Pool was created, which can be null
	 * if created via an Partition
	 */
	public Image getImage() {
		checkClosed();
		return image;
	}

	public long sectorOffset() {
		return sectorOffset;
	}

	/**
	 * @return Partition from which this Pool was created, which can be null
	 * if created via an Image
	 */
	public Partition getPartition() {
		checkClosed();
		return partition;
	}

	public List<Image> getVolumes() {
		checkClosed();
		if ( volumes == null ) {
			volumes = new ArrayList<>();
			final int numVolumes = countVolumes();
			for (int i=0; i<numVolumes; i++) {
				final long np = openVolume( nativePtr, i );
				if (np != 0) {
					final Image volume = new Image(np);
					volumes.add(volume);
				} else {
					volumes.add(null); // Keep meaningful volume indices
				}
			}
		}
		return Collections.unmodifiableList(volumes);
	}

	public int countVolumes() {
		checkClosed();
		return countVolumes( nativePtr );
	}
	
	@Override
	protected void closeImpl() {
		if ( volumes != null ) {
			for ( Image volume : volumes) {
				if ( volume != null ) {
					volume.close();
				}
			}
		}
		close( nativePtr );
		if( ownsImage )
			image.close();
	}

	/**
	 * @return image.getPath or null if this Pool was constructed from a Partition
	 */
	public String getPath() {
		// see note in Image.getPath
		return image == null ? null : image.getPath();
	}

	public long nativePtr() {
		checkClosed();
		return nativePtr;
	}

	public int getType() {
		checkClosed();
		return type( nativePtr );
	}

	public String typeDescription() {
		checkClosed();
		return type2Name( getType() );
	}

	static public native String type2Name( int type );

	private native long openImage( long imgNativePtr, long offset );
	private native long openPartition( long partitionNativePtr );
	private native int countVolumes( long nativePtr );
	private native long openVolume( long nativePtr, int volumeIndex );
	private native void close( long nativePtr );
	private native int type( long nativePtr );

	private final Image image;
	private final boolean ownsImage;
	private final long sectorOffset;
	private final Partition partition;
	private List<Image> volumes = null;
	private final long nativePtr;

}

// eof

	