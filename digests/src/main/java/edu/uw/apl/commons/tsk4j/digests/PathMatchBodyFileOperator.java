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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uw.apl.commons.tsk4j.filesys.FileSystem;

/**
   Given a regex pattern (which may have previously been formed via
   some glob), apply it to all BodyFile.Record.path fields, a
   filtering operation.  Use the 'apply' function to do this, defined
   in the superclass, thus:

   PathMatchBodyFileOperator pmo = new PathMatchBodyFileOperator(/WINDOWS/.*);
   BodyFile bf2 = pmo.apply( bf1 );

   or, via a convenience routine:

   BodyFile bf2 = PathMatchBodyFileOperator.apply( pattern, bf1 );

*/

public class PathMatchBodyFileOperator extends BodyFileUnaryOperator {

	/**
	   Convenience routine, builds and uses a local
	   PathMatchBodyFileOperator
	*/
	static public BodyFile apply( String pattern, BodyFile bf ) {
		PathMatchBodyFileOperator pmo =
			new PathMatchBodyFileOperator( pattern );
		return pmo.apply( bf );
	}
	
	public PathMatchBodyFileOperator( String pattern ) {
		super( "path ~ " + pattern, predicate( pattern ) );
	}

	static BodyFileUnaryOperator.Predicate predicate( String s ) {
		final Pattern p = Pattern.compile( s );
		BodyFileUnaryOperator.Predicate result =
			new BodyFileUnaryOperator.Predicate() {
				public boolean accepts( BodyFile.Record r, FileSystem fs ) {
					Matcher m = p.matcher( r.path );
					return m.matches();
				}
			};
		return result;
	}
}

// eof
		