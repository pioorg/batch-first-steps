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

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

class CumulativeValidator extends ItemStreamSupport implements Validator<Long> {
    private long currentSum = 0;

    @Override
    public void open(ExecutionContext executionContext) {
        if (executionContext.containsKey(getCurrentSumKey(executionContext))) {
            currentSum = executionContext.getLong(getCurrentSumKey(executionContext));
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        executionContext.putLong(getCurrentSumKey(executionContext), currentSum);
    }

    @Override
    public void validate(Long value) throws ValidationException {
        if (currentSum + value < 0) {
            throw new ValidationException(String.format("Value [%d] makes current sum [%d] go below zero", value, currentSum));
        }
        currentSum += value;
    }

    private String getCurrentSumKey(ExecutionContext executionContext) {
        return getExecutionContextKey("currentSum");
    }
}
