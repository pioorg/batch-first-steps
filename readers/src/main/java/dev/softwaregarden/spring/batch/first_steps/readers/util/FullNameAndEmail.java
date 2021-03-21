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

package dev.softwaregarden.spring.batch.first_steps.readers.util;

import java.util.Objects;
import java.util.StringJoiner;

public class FullNameAndEmail {
    private String fullName;
    private String email;

    public FullNameAndEmail(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public FullNameAndEmail() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullNameAndEmail that = (FullNameAndEmail) o;
        return fullName.equals(that.fullName) && email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, email);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FullNameAndEmail.class.getSimpleName() + "[", "]")
            .add("fullName='" + fullName + "'")
            .add("email='" + email + "'")
            .toString();
    }
}
