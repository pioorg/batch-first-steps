/*
 *
 *  Copyright (C) 2021 Piotr Przybył
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package dev.softwaregarden.spring.batch.first_steps.itemstream;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamSupport;

class DummyLongItemReader extends ItemStreamSupport implements ItemReader<Long> {
    private final List<Long> source = List.of(0L, 1L, 2L, 5L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    private int position = 0;

    @Override
    public Long read() {
        if (position >= source.size()) {
            System.out.println("No more items to read, returning null.");
            return null;
        }
        Long nextItem = source.get(position++);
        System.out.println("Read item [" + nextItem + "].");
        return nextItem;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        if (executionContext.containsKey(getCurrentSumKey(executionContext))) {
            position = executionContext.getInt(getCurrentSumKey(executionContext));
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        executionContext.putInt(getCurrentSumKey(executionContext), position);
    }

    private String getCurrentSumKey(ExecutionContext executionContext) {
        return getExecutionContextKey("position");
    }
}

