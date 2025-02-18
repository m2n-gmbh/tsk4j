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

import java.util.List;

import edu.uw.apl.commons.tsk4j.base.Utils;
import edu.uw.apl.commons.tsk4j.image.Image;
import edu.uw.apl.commons.tsk4j.volsys.Partition;
import edu.uw.apl.commons.tsk4j.volsys.VolumeSystem;

public class FileSystemTest extends junit.framework.TestCase {

	public void testSz1() throws Exception {

		String path = "/dev/sda";
		FileSystem fs1 = new FileSystem( path, 2048L );
		report( fs1 );
		fs1.close();
	}

	public void testSz2() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs1 = new FileSystem( path );
		report( fs1 );
		fs1.close();
	}

	// same as sz2, by design...
	public void testSz3() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs1 = new FileSystem( path );
		report( fs1 );
		fs1.close();
	}

	public void testPartitionsLinux() throws Exception {
		Image i = new Image( "/dev/sda" );
		VolumeSystem vs = new VolumeSystem( i );
		List<Partition> ps = vs.getPartitions();
		for( Partition p : ps ) {
			if( !p.isAllocated() )
				continue;
			if( p.description().contains( "Swap" ) )
				continue;
			report( p );
			FileSystem fs = new FileSystem( p );
			report( fs );
		}
	}

	public void testPartitionsWindows() throws Exception {
		java.io.File f = new java.io.File( "data/nuga2.dd" );
		if( !f.exists() )
			return;
		Image i = new Image( f );
		VolumeSystem vs = new VolumeSystem( i );
		List<Partition> ps = vs.getPartitions();
		for( Partition p : ps ) {
			if( !p.isAllocated() )
				continue;
			if( p.description().contains( "Swap" ) )
				continue;
			report( p );
			FileSystem fs = new FileSystem( p );
			report( fs );
		}
	}

	private void report( Partition p ) {
		System.out.println();
		System.out.println( "Address: " + p.address() );
		System.out.println( "Description: " + p.description() );
		System.out.println( "Flags: " + p.flags() );
		System.out.println( "Start: " + p.start() );
		System.out.println( "Length: " + p.length() );
		System.out.println( "Table/Slot: " + p.table() + "/" + p.slot());
		System.out.println( "IsAllocated?: " + p.isAllocated() );
	}
	
	private void report( FileSystem fs ) throws Exception {
		String path = fs.getPath();
		int type = fs.type();
		System.out.println( path + ": type " + type );
		long bc = fs.blockCount();
		System.out.println( path + ": blockCount " + bc );
		long bs = fs.blockSize();
		System.out.println( path + ": blockSize " + bs );
		long fb = 0;//fs.firstBlock();
		System.out.println( path + ": firstBlock " + fb );
		long fi = 0;//fs.firstINum();
		System.out.println( path + ": firstINum " + fi );
	}

	public void _testRead() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs1 = new FileSystem( path );//, 2048L * 512 );
		int bs = fs1.blockSize();

		byte[] root = fs1.read( 0, bs );
		System.out.println( path + " " + root.length );
		String md5 = Utils.md5sum( root );
		System.out.println( md5 );

		fs1.close();
	}

	public void _testBlockGet() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs1 = new FileSystem( path );//, 2048L * 512 );
		int bs = fs1.blockSize();

		Block b = fs1.getBlock( 0 );
		String s = String.format( "%x", b.flags() );
		System.out.println( "Flags " + s );
		System.out.println( "Buf " + b.buf().length );
		String md5 = Utils.md5sum( b.buf() );
		System.out.println( md5 );

		fs1.close();
	}
		
	public void _testFileOpenMeta() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs = new FileSystem( path );//, 2048L * 512 );
		File f = fs.fileOpenMeta( 2 );
		System.out.println( f.paramString() );
		fs.close();
	}

	public void _testFileOpen() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs = new FileSystem( path );//, 2048L * 512 );
		File f = fs.fileOpen( "/root" );
		System.out.println( f.paramString() );
		fs.close();
	}
		
	public void testDirWalk() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs = new FileSystem( path );//, 2048L * 512 );
		DirectoryWalk.Callback cb = new DirectoryWalk.Callback() {
				public int apply( WalkFile f, String path ) {
					System.out.println( path );
					System.out.println( f.getName() );
					//					System.out.println( f.paramString() );
					//f.close();
					return Walk.WALK_CONT;
					//					return FileSystem.Listener.WALKSTOP;
				}
			};
		//	fs.dirWalk( 2, DirWalk.FLAG_RECURSE, l );
	}

	public void _testMetaWalk() throws Exception {

		String path = "/dev/sda1";
		FileSystem fs = new FileSystem( path );//, 2048L * 512 );
		MetaWalk.Callback cb = new MetaWalk.Callback() {
				public int apply( WalkFile f ) {
					Meta m = f.meta();
					if( m != null )
						System.out.println( m.paramString() );
					
					System.out.println( f.getName() );
					return Walk.WALK_CONT;
				}
			};
		int flags = 0;
		fs.metaWalk( 2, 3, flags, cb );
	}

}

// eof
