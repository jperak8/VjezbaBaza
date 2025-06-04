import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Scanner;

public class VjezbaBaza {

    public static void main(String[] args) throws SQLException {

        Scanner scanner = new Scanner(System.in);
        DataSource dataSource = createDataSource();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Uspješno spojeni na bazu podataka!");


        } catch (SQLException e) {
            System.err.println("Greška prilikom spajanja na bazu podataka:");
            e.printStackTrace();
        }

        while (true) {
            System.out.println("\n=== IZBORNIK ZA RAD S DRŽAVAMA - Odaberite opciju [1-5]: ===");
            System.out.println("1 – nova država");
            System.out.println("2 – izmjena postojeće države");
            System.out.println("3 – brisanje postojeće države");
            System.out.println("4 – prikaz svih država sortiranih po nazivu)");
            System.out.println("5 – kraj");

            String unos = scanner.nextLine().trim();
            int opcija;
            try {
                opcija = Integer.parseInt(unos);
            } catch (NumberFormatException e) {
                System.out.println("Nevažeći unos, molim unesite broj između 1 i 5.");
                continue;
            }

            switch (opcija) {
                case 1:
                    dodajDrzavu(scanner);
                    break;
                case 2:
                    izmijeniDrzavu(scanner);
                    break;
                case 3:
                    obrisiDrzavu(scanner);
                    break;
                case 4:
                    prikaziSveDrzave();
                    break;
                case 5:
                    System.out.println("Kraj programa. Doviđenja!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Molim odaberite opciju između 1 i 5.");
            }
        }
    }

    private static DataSource createDataSource() {
        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setServerName("localhost");
        dataSource.setPortNumber(58196);
        dataSource.setDatabaseName("AdventureWorksOBP");
        dataSource.setUser("sa");
        dataSource.setPassword("SQL");
        dataSource.setEncrypt(false);
        return dataSource;
    }

    private static void dodajDrzavu(Scanner scanner) {
        System.out.print("Unesite naziv nove države: ");
        String naziv = scanner.nextLine().trim();
        String sqlAddCountry = "INSERT INTO Drzava (Naziv) VALUES (?)";

        try (Connection conn = createDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAddCountry, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("Uspješno spojeni na bazu podataka!");

            ps.setString(1, naziv);
            int redova = ps.executeUpdate();

            if (redova > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generiraniId = rs.getInt(1);
                        System.out.println("Država je uspješno dodana sa ID-em: " + generiraniId);
                    } else {
                        System.out.println("Nije moguće dohvatiti generirani ID.");
                    }
                }
            } else {
                System.out.println("Došlo je do pogreške pri dodavanju.");
            }

        } catch (SQLException e) {
            System.err.println("Greška pri radu s bazom: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private static void izmijeniDrzavu(Scanner scanner) throws SQLException {
        System.out.print("Unesite ID države za izmjenu: ");
        String idUnos = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idUnos);
        } catch (NumberFormatException e) {
            System.out.println("Nevažeći ID.");
            return;
        }

        System.out.print("Unesite novi naziv države: ");
        String noviNaziv = scanner.nextLine().trim();

        String sqlUpdateCountry = "UPDATE Drzava SET Naziv = ? WHERE idDrzava = ?";
        try (Connection conn = createDataSource().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sqlUpdateCountry);
            ps.setString(1, noviNaziv);
            ps.setInt(2, id);
            int redova = ps.executeUpdate();
            if (redova > 0) {
                System.out.println("Država je uspješno ažurirana.");
            } else {
                System.out.println("Država s tim ID-em ne postoji.");
            }
        } catch (SQLException e) {
            System.out.println("Greška u bazi: " + e.getMessage());
        }
    }

    private static void obrisiDrzavu(Scanner scanner) {
        System.out.print("Unesite ID države za brisanje: ");
        String idUnos = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idUnos);
        } catch (NumberFormatException e) {
            System.out.println("Nevažeći ID.");
            return;
        }

        String sqlDeleteCountry = "DELETE FROM Drzava WHERE idDrzava = ?";
        try (Connection conn = createDataSource().getConnection()){
            PreparedStatement ps = conn.prepareStatement(sqlDeleteCountry);
            ps.setInt(1, id);
            int redova = ps.executeUpdate();
            if (redova > 0) {
                System.out.println("Država je uspješno obrisana.");
            } else {
                System.out.println("Država s tim ID-em ne postoji.");
            }
        } catch (SQLException e) {
            System.out.println("Greška u bazi: " + e.getMessage());
        }
    }

    private static void prikaziSveDrzave() {
        String sqlSelectAllCountries = "SELECT idDrzava, Naziv FROM Drzava ORDER BY Naziv ASC";
        try (Connection conn = createDataSource().getConnection()){
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlSelectAllCountries);

            System.out.println("\n--- SVE DRŽAVE U BAZI ISPISANE ABECEDNIM REDOM ---");
            while (rs.next()) {
                int id = rs.getInt("idDrzava");
                String naziv = rs.getString("Naziv");
                System.out.printf("%3d  %s%n", id, naziv);
            }

        } catch (SQLException e) {
            System.out.println("Greška pri dohvaćanju podataka: " + e.getMessage());
        }
    }
}