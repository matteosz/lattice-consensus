package cs451;

import cs451.parser.Parser;
import cs451.service.CommunicationService;

/**
 * @package cs451
 *
 * @author Matteo Suez <matteo.suez@epfl.ch>
 * @date 10.12.2022
 * @section LICENSE
 *
 * Copyright © 2022-2023
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version. Please see https://gnu.org/licenses/gpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * @section DESCRIPTION
 *
 * This project implements a weak form of consensus (Lattice Consensus)
 * among a constrained number of processes (128 at most). It implements
 * also a stack of underlying abstractions, from the FairLoss link to
 * the Best Effort Broadcast to serve the purpose.
 */

/**
 * Main class:
 *
 * Starting point of the program.
 *
 */
public class Main {

    /**
     * Add a function as shutdown hook, in order to be
     * trigger by SIGINT or SIGTERM events
     */
    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(CommunicationService::logAndTerminate));
    }

    /**
     * Main function to which pass args to be parsed.
     * @param args command line arguments
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        // Assign the shutdown hook
        initSignalHandlers();

        // Parse the command line arguments, the host and the config files
        Parser.parse(args);

        long pid = ProcessHandle.current().pid();
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        // Start the communication service
        CommunicationService.start();
    }
}
