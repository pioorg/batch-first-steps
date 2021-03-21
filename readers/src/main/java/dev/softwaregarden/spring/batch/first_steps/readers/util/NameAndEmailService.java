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

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Please keep in mind, that this service is for demo purposes only. Do not create actual services like that, please.
 */
@Service
public class NameAndEmailService {
    private final List<NameAndEmail> items = List.of(
        new NameAndEmail("John", "Doe", "john.doe@example.com"),
        new NameAndEmail("John", "McClay", "j_mcclay@example.com"),
        new NameAndEmail("Ellen", "Ripley", "alien@area51.example.com"),
        new NameAndEmail("Vito", "Corleone", "Don_Corleone@example.com"),
        new NameAndEmail("Mia", "Wallace", "mwallace@example.co")
    );
    private int position = 0;

    public NameAndEmail fetchNameAndEmail() {
        if (position < items.size()) {
            return items.get(position++);
        }
        return null;
    }


}
