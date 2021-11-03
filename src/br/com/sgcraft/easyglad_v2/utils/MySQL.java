package br.com.sgcraft.easyglad_v2.utils;

import java.sql.*;
import org.bukkit.entity.*;

import br.com.sgcraft.easyglad_v2.*;

public class MySQL
{
    private String user;
    private String password;
    private String database;
    private String host;
    private Connection connection;
    private Statement stmt;
    public static Main pl;
    
    static {
        MySQL.pl = Main.pl;
    }
    
    public MySQL(final String user, final String password, final String database, final String host) {
        try {
            this.user = user;
            this.password = password;
            this.database = database;
            this.host = host;
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, user, password);
            (this.stmt = this.connection.createStatement()).execute("CREATE TABLE IF NOT EXISTS glad (clan VARCHAR(255), wins INTEGER)");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addNew(final String clan, final int wins) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            final String sql = "INSERT INTO glad (clan, wins) VALUES ('" + clan + "', '" + wins + "');";
            this.stmt.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateWins(final String clan, final int wins) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            final String sql = "UPDATE glad SET wins='" + (this.getWins(clan) + wins) + "' WHERE clan='" + clan + "';";
            this.stmt.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int getWins(final String clan) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            final String sql = "SELECT wins FROM glad WHERE clan='" + clan + "';";
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
            Class.forName("com.mysql.jdbc.Driver");
            final String sql = "SELECT * FROM glad WHERE clan='" + clan + "'";
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
            p.sendMessage(MySQL.pl.getConfig().getString("Top_Vencedores").replace("&", "§"));
            Class.forName("com.mysql.jdbc.Driver");
            final String sql = "SELECT * FROM glad ORDER BY wins DESC LIMIT 10";
            final ResultSet rs = this.stmt.executeQuery("SELECT * FROM glad ORDER BY wins DESC LIMIT 10");
            int i = 0;
            while (rs.next()) {
                ++i;
                p.sendMessage(MySQL.pl.getConfig().getString("Top_Vencedores_Posicao").replace("&", "§").replace("@posicao", new StringBuilder(String.valueOf(i)).toString()).replace("@clan", rs.getString("clan")).replace("@vitorias", new StringBuilder(String.valueOf(rs.getInt("wins"))).toString()));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addWinnerPoint(final String clan) {
        if (this.hasClan(clan)) {
            this.updateWins(clan, 1);
        }
        else {
            this.addNew(clan, 1);
        }
    }
    
    public void purgeRows() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            final String sql = "DELETE FROM glad;";
            this.stmt.executeUpdate("DELETE FROM glad;");
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
