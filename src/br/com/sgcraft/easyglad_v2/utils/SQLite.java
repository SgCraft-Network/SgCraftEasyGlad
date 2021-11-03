package br.com.sgcraft.easyglad_v2.utils;

import java.io.*;
import java.sql.*;
import org.bukkit.entity.*;

import br.com.sgcraft.easyglad_v2.*;

public class SQLite
{
    private String user;
    private String password;
    private String database;
    private String host;
    private Connection connection;
    private Statement stmt;
    public static Main pl;
    
    static {
        SQLite.pl = Main.pl;
    }
    
    public SQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + SQLite.pl.getDataFolder().getAbsolutePath() + File.separator + "database.db");
            (this.stmt = this.connection.createStatement()).execute("CREATE TABLE IF NOT EXISTS topClans (clan VARCHAR(255), clanTag VARCHAR(255), wins INTEGER)");
            (this.stmt = this.connection.createStatement()).execute("CREATE TABLE IF NOT EXISTS topKills (player VARCHAR(255), kills INTEGER)");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }
    }
    
    public void addNew(final String clan, final String clanTag, final int wins) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "INSERT INTO topClans (clan, clanTag, wins) VALUES ('" + clan + "','" + clanTag + "', '" + wins + "');";
            this.stmt.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateWins(final String clan, final String clanTag, final int wins) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "UPDATE topClans SET wins='" + (this.getWins(clan) + wins) + "', clanTag='" + clanTag + "' WHERE clan='" + clan + "';";
            this.stmt.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int getWins(final String clan) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "SELECT wins FROM topClans WHERE clan='" + clan + "';";
            final ResultSet rs = this.stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("wins");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public boolean hasClan(final String clan) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "SELECT * FROM topClans WHERE clan='" + clan + "'";
            final ResultSet rs = this.stmt.executeQuery(sql);
            return rs.next() && rs.getString("clan").equalsIgnoreCase(clan);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void getTOPWins(final Player p) {
        try {
            p.sendMessage(SQLite.pl.getConfig().getString("Top_Vencedores").replace("&", "§"));
            Class.forName("org.sqlite.JDBC");
            final ResultSet rs = this.stmt.executeQuery("SELECT * FROM topClans ORDER BY wins DESC LIMIT 10");
            int i = 0;
            while (rs.next()) {
                if (++i == 0) {
                    p.sendMessage("§6[MiniGladiador] §cNenhum clan ganhou o gladiador ainda!");
                }
                else {
                    p.sendMessage(SQLite.pl.getConfig().getString("Top_Vencedores_Posicao").replace("&", "§").replace("@posicao", new StringBuilder(String.valueOf(i)).toString()).replace("@clan", rs.getString("clanTag")).replace("@vitorias", new StringBuilder(String.valueOf(rs.getInt("wins"))).toString()));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addWinnerPoint(final String clan, final String clanTag) {
        if (this.hasClan(clan)) {
            this.updateWins(clan, clanTag, 1);
        }
        else {
            this.addNew(clan, clanTag, 1);
        }
    }
    
    public void addTopKills(final String player, final int kills) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "INSERT INTO topKills (player, kills) VALUES ('" + player + "','" + kills + "');";
            this.stmt.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateKills(final String player, final int kills) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "UPDATE topKills SET kills='" + (this.getKills(player) + kills) + "' WHERE player='" + player + "';";
            this.stmt.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int getKills(final String player) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "SELECT kills FROM topKills WHERE player='" + player + "';";
            final ResultSet rs = this.stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("kills");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public boolean hasPlayer(final String player) {
        try {
            Class.forName("org.sqlite.JDBC");
            final String sql = "SELECT * FROM topKills WHERE player='" + player + "'";
            final ResultSet rs = this.stmt.executeQuery(sql);
            return rs.next() && rs.getString("player").equalsIgnoreCase(player);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void getTOPKills(final Player p) {
        try {
            p.sendMessage(SQLite.pl.getConfig().getString("Top_Kills").replace("&", "§"));
            Class.forName("org.sqlite.JDBC");
            final ResultSet rs = this.stmt.executeQuery("SELECT * FROM topKills ORDER BY kills DESC LIMIT 10");
            int i = 0;
            while (rs.next()) {
                if (++i == 0) {
                    p.sendMessage("§6[MiniGladiador] §cNenhum player no TopKills!");
                }
                else {
                    p.sendMessage(SQLite.pl.getConfig().getString("Top_Kills_Posicao").replace("&", "§").replace("@posicao", new StringBuilder(String.valueOf(i)).toString()).replace("@player", rs.getString("player").toUpperCase()).replace("@kills", new StringBuilder(String.valueOf(rs.getInt("kills"))).toString()));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addKillerPoint(final String player) {
        if (this.hasPlayer(player)) {
            this.updateKills(player, 1);
        }
        else {
            this.addTopKills(player, 1);
        }
    }
    
    public void resetGladTop() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.stmt.executeUpdate("DELETE FROM topClans;");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void resetTopKills() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.stmt.executeUpdate("DELETE FROM topKills;");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getUser() {
        return this.user;
    }
    
    public void setUser(final String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(final String password) {
        this.password = password;
    }
    
    public String getDatabase() {
        return this.database;
    }
    
    public void setDatabase(final String database) {
        this.database = database;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public void setHost(final String host) {
        this.host = host;
    }
    
    public Connection getConnection() {
        return this.connection;
    }
    
    public void setConnection(final Connection connection) {
        this.connection = connection;
    }
    
    public Statement getStmt() {
        return this.stmt;
    }
    
    public void setStmt(final Statement stmt) {
        this.stmt = stmt;
    }
}
