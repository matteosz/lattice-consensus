package cs451;

import cs451.parser.Parser;
import cs451.service.CommunicationService;
import java.io.IOException;

/**
 * @package cs451
 *
 * @author Matteo Suez <matteo.suez@epfl.ch>
 * @date 12.2022
 * @section LICENSE
 *
 * Copyright © 2022-2023
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version. Please see <a href="https://gnu.org/licenses/gpl.html">GPLv3</a>
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * @section DESCRIPTION
 *
 * This project implements a weak form of consensus (Lattice Consensus)
 * among a constrained number of processes (128 at most). To serve the
 * purpose it implements also a stack of underlying abstractions,
 * from the Fair-Loss link to the Best-Effort Broadcast.
 *
 */
public class Main {

    /**
     * Add a function as shutdown hook, in order to be
     * triggered by SIGINT or SIGTERM events.
     */
    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(CommunicationService::logAndTerminate));
    }

    /**
     * Main function to which pass args to be parsed.
     * @param args command line arguments.
     */
    public static void main(String[] args) throws IOException {
        // Assign the shutdown hook
        initSignalHandlers();

        // Parse the command line arguments, the host and the config files
        Parser.parse(args);

        // Start the communication service
        CommunicationService.start();
    }

}
