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

import java.util.*;

import edu.uw.apl.commons.tsk4j.base.Utils;
import edu.uw.apl.commons.tsk4j.image.Image;

public class BlockWalkTest extends junit.framework.TestCase {

	public void testSDA1() throws Exception {
		String path = "/dev/sda1";
		FileSystem fs = new FileSystem( path );
		test( fs );
		fs.close();
	}
	
	public void testNuga2() throws Exception {
		String path = "data/nuga2.dd";
		java.io.File f = new java.io.File( path );
		if( !f.exists() )
			return;
		FileSystem fs = new FileSystem( path, 63 );
		test( fs );
		fs.close();
	}
	
	public void test( FileSystem fs ) throws Exception {
		System.out.println( "BlockSize: " + fs.blockSize() );
		System.out.println( "BlockCount: " + fs.blockCount() );
		if( false && fs.blockCount() * fs.blockSize() >
			1024L * 1024 * 1024 * 16 )
			return;
		printBlockWalk( fs );
		if( true )
			return;
		allocedBlocks( fs );
		fs.close();
	}

	private void printBlockWalk( FileSystem fs ) throws Exception {
		BlockWalk.Callback cb = new BlockWalk.Callback() {
				public int apply( BlockWalk.Block b ) {
					System.out.println( b.addr() + " " +
										b.decodeFlags() );
					return Walk.WALK_CONT;
				}
			};
		int flags = BlockWalk.FLAG_NONE;
		fs.blockWalk( fs.firstBlock(), fs.lastBlock(), flags, cb );
	}

	private void allocedBlocks( FileSystem fs ) throws Exception {
		BlockWalk.Callback cb = new BlockWalk.Callback() {
				public int apply( BlockWalk.Block b ) {
					System.out.println( b.addr() + " " +
										b.decodeFlags() );
					return Walk.WALK_CONT;
				}
			};
		int flags = BlockWalk.FLAG_ALLOC| BlockWalk.FLAG_AONLY;
		fs.blockWalk( fs.firstBlock(), fs.lastBlock(), flags, cb );
	}
}

// eof
