/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package org.netbeans.lib.profiler.charts.axis;

import java.util.Iterator;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.Timeline;

/**
 *
 * @author Jiri Sedlacek
 */
public class TimelineMarksComputer extends AxisMarksComputer.Abstract {

    private final Timeline timeline;

    private double scale;
    private long step;

    private long firstTimestamp;
    private long lastTimestamp;


    public TimelineMarksComputer(Timeline timeline,
                                 ChartContext context,
                                 int orientation) {

        super(context, orientation);
        this.timeline = timeline;

        scale = -1;
        step = -1;
    }


    protected int getMinMarksDistance() {
            return 120;
        }

    protected boolean refreshConfiguration() {
        double oldScale = scale;
        long oldFirstTimestamp = firstTimestamp;
        long oldLastTimestamp = lastTimestamp;
        
        if ((horizontal && context.getViewWidth() == 0) ||
            (!horizontal && context.getViewHeight() == 0)) {
            scale = -1;
//        } else if (timeline.getTimestampsCount() == 0) {
//            // Initial scale
//            scale = -1;
        } else {
            scale = horizontal ? context.getViewWidth(1d) :
                                 context.getViewHeight(1d);
        }

        int timestampsCount = timeline.getTimestampsCount();
        if (horizontal) {
            firstTimestamp = timestampsCount == 0 ? (long)context.getDataX(0) :
                                                     timeline.getTimestamp(0);
            lastTimestamp = timestampsCount == 0 ? (long)context.getDataX(
                                                    context.getViewportWidth()):
                                                    Math.max(timeline.getTimestamp
                                                    (timestampsCount - 1),
                                                    (long)context.getDataX(
                                                    context.getViewportWidth()));
        } else {
            firstTimestamp = timestampsCount == 0 ? (long)context.getDataY(0) :
                                                     timeline.getTimestamp(0);
            lastTimestamp = timestampsCount == 0 ? (long)context.getDataY(
                                                    context.getViewportWidth()):
                                                    Math.max(timeline.getTimestamp
                                                    (timestampsCount - 1),
                                                    (long)context.getDataY(
                                                    context.getViewportWidth()));
        }
        
        if (oldScale != scale) {

            if (scale == -1) {
                step = -1;
            } else {
                step = TimeAxisUtils.getTimeUnits(scale, getMinMarksDistance());
            }

            oldScale = scale;
            return true;
        } else {
            return oldFirstTimestamp != firstTimestamp ||
                   oldLastTimestamp != lastTimestamp;
        }
    }


    public Iterator<AxisMark> marksIterator(int start, int end) {
        if (step == -1) return EMPTY_ITERATOR;

        final long dataStart = horizontal ?
                               ((long)context.getDataX(start) / step) * step :
                               ((long)context.getDataY(start) / step) * step;
        final long dataEnd = horizontal ?
                               ((long)context.getDataX(end) / step) * step :
                               ((long)context.getDataY(end) / step) * step;
        final long iterCount = Math.abs(dataEnd - dataStart) / step + 2;
        final long[] iterIndex = new long[] { 0 };

        final String format = TimeAxisUtils.getFormatString(step, firstTimestamp,
                                                            lastTimestamp);


        return new AxisMarksComputer.AbstractIterator() {

            public boolean hasNext() {
                return iterIndex[0] < iterCount;
            }

            public AxisMark next() {
                long value = reverse ? dataStart - iterIndex[0] * step :
                                       dataStart + iterIndex[0] * step;
                iterIndex[0]++;
                int position = horizontal ?
                               ChartContext.getCheckedIntValue(
                                            context.getViewX(value)) :
                               ChartContext.getCheckedIntValue(
                                            context.getViewY(value));
                return new TimeMark(value, position, format);
            }

        };
    }

}
