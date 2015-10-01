/**
 * Copyright (C) 2010
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH"
 * (Know-Center), Graz, Austria, office@know-center.at.
 *
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.knowcenter.code.pdf.blockclassification.detection.metadata.utils;

/**
 * 
 * 
 * @author Roman Kern <rkern@know-center.at>
 */
public class TextSpan {
    private final String text;
    private final int position;
    private final int start;
    private final int end;

    /**
     * Creates a new instance of this class.
     * @param textCaseAligned
     */
    public TextSpan(String textCaseAligned) {
        this.text = textCaseAligned;
        this.position = -1;
        this.start = -1;
        this.end = -1;
    }

    /**
     * Creates a new instance of this class.
     * @param text 
     * @param index 
     * @param start 
     * @param end 
     */
    public TextSpan(String text, int index, int start, int end) {
        this.text = text;
        this.position = index;
        this.start = start;
        this.end = end;
    }

    /**
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     * @return
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return getText();
    }
}
