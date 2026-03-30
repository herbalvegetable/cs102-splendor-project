package src.com.splendor.web.game;

/**
 * One seat at the table in turn order: index 0 is youngest (starts first), last is oldest.
 *
 * @param name display name (may be blank; server substitutes "Player N")
 * @param human true for human, false for CPU
 */
public record PlayerSetup(String name, boolean human) {}
