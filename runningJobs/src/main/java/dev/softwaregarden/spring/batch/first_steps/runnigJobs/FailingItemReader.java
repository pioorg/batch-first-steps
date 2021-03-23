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

package dev.softwaregarden.spring.batch.first_steps.runnigJobs;

import org.springframework.batch.item.ItemReader;

class FailingItemReader implements ItemReader<Long> {
    long repeatsLeft;

    public FailingItemReader(Long failWith) {
        repeatsLeft = failWith;
    }

    @Override
    public Long read() throws Exception {
        if (repeatsLeft-- > 0) {
            System.err.printf("Still working%n");
//                Thread.sleep(100L);
            return repeatsLeft;
        }
        System.err.println("KABOOM!");
        throw new IllegalStateException("KABOOM!");
    }
}
