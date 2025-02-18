/**
 * Copyright © 2015, University of Washington
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
package edu.uw.apl.commons.tsk4j.filesys;

import java.util.ArrayList;
import java.util.List;

import edu.uw.apl.commons.tsk4j.base.Closeable;

/**
 * @author Stuart Maclean
 *
 * Java wrapper around the Sleuthkit TSK_FS_BLOCK struct and api.
 *
 * {@link http://www.sleuthkit.org/sleuthkit/docs/api-docs/fspage.html}
 *
 * A block is the unit of storage within a filesystem, usually 4K or
 * similar. The actual value is a property of the parent FileSystem.
 *
 * A block has an address, contents and flags (allocated,
 * unallocated, etc)
 *
 * @see FileSystem
 */

public class Block extends Closeable {

	// Called only by native code: filesystem.c (and BlockWalk$Block subclass)
	Block( long nativePtr, FileSystem fs ) {
		this.nativePtr = nativePtr;
		this.fs = fs;
	}

	protected void closeImpl() {
		free( nativePtr );
	}
	
	/**
	 * Calls (via close) through to tsk_fs_block_free
	 */
	public void free() {
		close();
	}

	/**
	 * @result the FileSystem containing this block
	 */
	public FileSystem getFileSystem() {
		return fs;
	}
	
	/**
	 * @result TSK_FS_BLOCK->addr
	 */
	public long addr() {
		checkClosed();
		return addr( nativePtr );
	}
	
	/**
	 * @result new byte[], populated with data in TSK_FS_BLOCK->buf
	 */
	public byte[] buf() {
		checkClosed();
		byte[] result = new byte[fs.blockSize()];
		buf( result );
		return result;
	}

	/**
	 * @result as above, but byte[] passed in, and
	 * populated with data in TSK_FS_BLOCK->buf
	 */
	public void buf( byte[] result ) {
		checkClosed();
		if( result.length < fs.blockSize() )
			throw new IllegalArgumentException
				( "Buffer not at least FS blockSize: " + fs.blockSize() );
		buf( nativePtr, fs.blockSize(), result );
	}
		

	/**
	 * @result TSK_FS_BLOCK->flags
	 */
	public int flags() {
		checkClosed();
		return flags( nativePtr );
	}

	public List<String> decodeFlags() {
		return decodeFlags( flags() );
	}
	
	final long nativePtr;
	final FileSystem fs;

	private native void free( long nativePtr );
	private native long addr( long nativePtr );
	private native int flags( long nativePtr );
	private native void buf( long nativePtr, int blockSize, byte[] result );

	static public List<String> decodeFlags( int flags ) {
		List<String> result = new ArrayList<String>();
		if( flags == FLAG_UNUSED ) {
			result.add( "unused" );
			return result;
		}
		if( (flags & FLAG_ALLOC) == FLAG_ALLOC )
			result.add( "alloc" );
		if( (flags & FLAG_UNALLOC) == FLAG_UNALLOC )
			result.add( "unalloc" );
		if( (flags & FLAG_CONT) == FLAG_CONT )
			result.add( "content" );
		if( (flags & FLAG_META) == FLAG_META )
			result.add( "meta" );
		if( (flags & FLAG_BAD) == FLAG_BAD )
			result.add( "bad" );
		if( (flags & FLAG_RAW) == FLAG_RAW )
			result.add( "raw" );
		if( (flags & FLAG_SPARSE) == FLAG_SPARSE )
			result.add( "sparse" );
		if( (flags & FLAG_COMP) == FLAG_COMP )
			result.add( "compressed" );
		if( (flags & FLAG_RES) == FLAG_RES )
			result.add( "resident" );
		return result;
	}
	
	public static final int FLAG_UNUSED	= 0x0;
	public static final int FLAG_ALLOC	= 0x1;
	public static final int FLAG_UNALLOC= 0x2;
	public static final int FLAG_CONT	= 0x4;
	public static final int FLAG_META	= 0x8;
	public static final int FLAG_BAD	= 0x10;
	public static final int FLAG_RAW	= 0x20;
	public static final int FLAG_SPARSE = 0x40;
	public static final int FLAG_COMP	= 0x80;
	public static final int FLAG_RES	= 0x100;
}

// eof
