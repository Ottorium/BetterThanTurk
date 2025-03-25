package at.htlhl.chess.gui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.engine.EvaluatedMove;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class UCIClient {

    private final String defaultThinkTime = "300";
    private Process process = null;
    private BufferedReader reader = null;
    private OutputStreamWriter writer = null;

    public UCIClient() {
    }

    /**
     * Looks if process is alive
     *
     * @return true if is alive
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * Starts the process
     *
     * @param cmd command name in console( f.e. "stockfish"). if the command is not in PATH env virable, the full path has to be defined
     */
    public void start(String cmd) throws IOException {
        var pb = new ProcessBuilder(cmd);
        this.process = pb.start();
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.writer = new OutputStreamWriter(process.getOutputStream());
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
    private void initialiseEngineUci() {
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
     * @param thinkTime time that engine will spend thinking smaller then 50000
     * @return Move best move
     */
    public Move getBestMove(String thinkTime) {
        String bestMove = "";
        try {
            bestMove = command(
                    "go movetime " + thinkTime,
                    lines -> lines.stream().filter(s -> s.startsWith("bestmove")).findFirst().get(),
                    line -> line.startsWith("bestmove"),
                    50000l)
                    .split(" ")[1];
            return Move.valueOf(bestMove);
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
        return getBestMove(defaultThinkTime);
    }

    /**
     * Gets the best Moves array, with
     *
     * @param thinkTime
     * @return
     */
    public List<EvaluatedMove> getBestMoves(String thinkTime) {
        // We set MultiPV to 10

        Map<Integer, EvaluatedMove> moves = new TreeMap<>();
        String analysisLineRegex = "info depth ([\\w]*) seldepth [\\w]* multipv ([\\w]*) score (cp ([\\-\\w]*)|mate ([\\w*])) [\\s\\w]*pv ([\\w]*)\\s*([\\s\\w]*)";
        final var pattern = Pattern.compile(analysisLineRegex);
        try {
            // To get 10 best moves
            command("setoption name MultiPV value 10", Function.identity(), s -> s.startsWith("readyok"), 2000l);
            moves = command(
                    "go movetime " + thinkTime,
                    lines -> {
                        Map<Integer, EvaluatedMove> result = new TreeMap<>();
                        for (String line : lines) {
                            var matcher = pattern.matcher(line);
                            if (matcher.matches()) {
                                Integer pv = Integer.parseInt(matcher.group(2));
                                String move = matcher.group(6);                  // Move (e.g., "e2e4")
                                String scoreType = matcher.group(3);            // "cp X" or "mate X"
                                double score;

                                if (scoreType.startsWith("cp")) {
                                    // Centipawn score (group 4 is the cp value)
                                    score = Double.parseDouble(matcher.group(4)) * 100.0; // Convert to pawns
                                } else {
                                    // Mate score (group 5 is the mate value)
                                    int mateMoves = Integer.parseInt(matcher.group(5));
                                    // Assign a large value, positive for positive mate, negative for negative
                                    score = mateMoves > 0 ? 10000.0 - mateMoves : -10000.0 - mateMoves;
                                }
                                result.put(pv, new EvaluatedMove(Move.valueOf(move), (int) score));
                            }
                        }
                        return result;
                    },
                    s -> s.startsWith("bestmove"),
                    50000l);
            // To clear settings
            command("setoption name MultiPV value 1", Function.identity(), s -> s.startsWith("readyok"), 2000l);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong while getting best Moves" + e.getMessage());
        } catch (TimeoutException e) {
            System.err.println("engine timeout");
        }
        List<EvaluatedMove> out = new ArrayList<>();
        out.addAll(moves.values());
        return out;
    }


    /**
     * Gets 10 best moves in given position
     *
     * @return
     */
    public List<EvaluatedMove> getBestMoves() {
        return getBestMoves(defaultThinkTime);
    }

    /**
     * sets stockfishe's threadcount to this number
     *
     * @param threadCount
     */
    public void setThreadCount(String threadCount) {
        try {
            command("setoption name Threads value " + threadCount, Function.identity(), s -> s.startsWith("readyok"),
                    2000l);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong while setting the Thread count " + e.getMessage());
        } catch (TimeoutException e) {
            System.err.println("engine timeout");
        }
    }
}

