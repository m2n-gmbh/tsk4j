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
package edu.uw.apl.commons.tsk4j.digests;

import java.io.InputStream;

import edu.uw.apl.commons.tsk4j.filesys.Attribute;
import edu.uw.apl.commons.tsk4j.filesys.File;
import edu.uw.apl.commons.tsk4j.filesys.FileSystem;

/**
   @author Stuart Maclean
   
   A unary operator which tests each BodyFile.Record to see if its main
   data attribute conforms to the Windows Portable Executable (PE) spec.

   This operator only makes sense when the full FileSystem from which
   the BodyFile was built is available, since only with a handle on
   the actual FileSystem can we inspect file content.  If a BodyFile's
   link to its parent FileSystem is null, which it would be if we load
   a BodyFile from its external representation (i.e. the saved output
   of fls) we (arbitrarily) say that this is NOT a WinPE file.
*/

public class WinPEOperator extends BodyFileUnaryOperator {

	public WinPEOperator() {
		super( "winpe?", WINPEP );
	}

	static final BodyFileUnaryOperator.Predicate WINPEP =
		new BodyFileUnaryOperator.Predicate() {
			public boolean accepts( BodyFile.Record r, FileSystem fs ) {
				/*
				  From the constraints imposed by tiny97.exe, the
				  craziest of all valid WinPE files.  We can test
				  this w/out even having a FileSystem.
				*/
				if( r.size < 97 )
					return false;

				/*
				  If no FileSystem, cannot proceed.  Say false?
				  Better to throw exception??
				*/
				if( fs == null )
					return false;
				
				File f = fs.fileOpenMeta( r.inode );
				Attribute a = f.getAttribute();
				return WinUtils.isWinPE( a );
			}
		};



}

// eof
		