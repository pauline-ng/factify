/**
    Copyright (C) 2016, Genome Institute of Singapore, A*STAR  

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.factpub.factify.pattern;

import java.util.List;

import org.factpub.factify.nlp.Sequence;
import org.factpub.factify.utility.Span;

/**
 * 
 * Interface for rule matching.
 *
 */
public interface Matcher {
	/**
	 * 
	 * @param senten A sequence representing a sentence
	 * @return A list of spans of substrings/subsequence that matches the rule
	 */
	List<Span> Match(Sequence senten);
}
