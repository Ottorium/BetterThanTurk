package at.htlhl.chess.gui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.concurrent.ExecutionException;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class UCIClient {

    private Process process = null;
    private BufferedReader reader = null;
    private OutputStreamWriter writer = null;

    public UCIClient() {
    }

    /**
     * Starts the process
     *
     * @param cmd command name in console( f.e. "stockfish"). if the command is not in PATH env virable, the full path has to be defined
     */
    public void start(String cmd) {
        var pb = new ProcessBuilder(cmd);
        try {
            this.process = pb.start();
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            this.writer = new OutputStreamWriter(process.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialiseEngineUci();
    }

    /**
     * Closes the process
     */
    public void close() {
        if (this.process.isAlive()) {
            this.process.destroy();
        }
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a command to the  process
     *
     * @param cmd              command string
     * @param commandProcessor function that translates List<String> to T
     * @param breakCondition   condition when we should stop reading response
     * @param timeout          long in milliseconds
     * @return <T> is defined by command Processor
     */
    public <T> T command(String cmd, Function<List<String>, T> commandProcessor, Predicate<String> breakCondition, long timeout)
            throws InterruptedException, ExecutionException, TimeoutException {

        // This completable future will send a command to the process
        // And gather all the output of the engine in the List<String>
        // At the end, the List<String> is translated to T through the
        // commandProcessor Function
        CompletableFuture<T> command = supplyAsync(() -> {
            final List<String> output = new ArrayList<>();
            try {
                writer.flush();
                writer.write(cmd + "\n");
                writer.write("isready\n");
                writer.flush();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Unknown command")) {
                        throw new RuntimeException(line);
                    }
                    if (line.contains("Unexpected token")) {
                        throw new RuntimeException("Unexpected token: " + line);
                    }
                    output.add(line);
                    if (breakCondition.test(line)) {
                        // At this point we are no longer interested to read any more
                        // output from the engine, we consider that the engine responded
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return commandProcessor.apply(output);
        });

        return command.get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Is used to tell engine process that we speak with uci
     */
    public void initialiseEngineUci() {
        try {
            command("uci", Function.identity(), (s) -> s.startsWith("uciok"), 2000l);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong while " + e.getMessage());
        } catch (TimeoutException e) {
            System.err.println("engine timeout");
        }
    }

    /**
     * Sets engine position to input
     *
     * @param fen position
     */
    public void setPosition(String fen) {
        try {
            command("position fen " + fen, Function.identity(), s -> s.startsWith("readyok"), 2000l);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong while setting position " + e.getMessage());
        } catch (TimeoutException e) {
            System.err.println("engine timeout");
        }
    }

    /**
     * Gets the best move in given position
     *
     * @param thinkTime time that engine will spend thinking
     * @return Move best move
     */
    public Move getBestMove(String thinkTime) {
        String bestMove = "";
        try {
            bestMove = command(
                    "go movetime " + thinkTime,
                    lines -> lines.stream().filter(s -> s.startsWith("bestmove")).findFirst().get(),
                    line -> line.startsWith("bestmove"),
                    5000l)
                    .split(" ")[1];
            Square startingSquare = Square.parseString(bestMove.substring(0, 2));
            Square endingSquare = Square.parseString(bestMove.substring(2, 4));
            return new Move(startingSquare, endingSquare);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong while getting best Move" + e.getMessage());
        } catch (TimeoutException e) {
            System.err.println("engine timeout");
        }
        return null;
    }

    /**
     * Gets the best move in given position
     *
     * @return Move best move
     */
    public Move getBestMove() {
        return getBestMove("3000");
    }
}

