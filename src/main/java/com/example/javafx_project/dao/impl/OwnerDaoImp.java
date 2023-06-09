package com.example.javafx_project.dao.impl;


import com.example.javafx_project.dao.OwnerDao;
import com.example.javafx_project.entities.Owner;
import javafx.collections.ObservableList;
import org.apache.poi.ss.formula.functions.Address;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OwnerDaoImp implements OwnerDao {

    private Connection conn= DB.getConnection();
    @Override
    public void insert(Owner owner) {

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement("INSERT INTO owner (Name,CIN,Address,PhoneNumber) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, owner.getName());
            ps.setString(2, owner.getCIN());
            ps.setString(3, owner.getAddress());
            ps.setInt(4, owner.getPhoneNumber());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int id = rs.getInt(1);
                    owner.setId(id);
                }
                DB.closeResultSet(rs);
            } else {
                System.out.println("Aucune ligne renvoyée");
            }
        } catch (SQLException e) {
            System.err.println("problème d'insertion d'un propriétaire");;
        } finally {
            DB.closeStatement(ps);
        }

    }

    @Override
    public void update(Owner owner) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE owner SET Name = ?,Cin = ?,Address = ?,PhoneNumber = ?  WHERE Id = ?");
            ps.setString(1, owner.getName());
            ps.setString(2,owner.getCIN());
            ps.setString(3,owner.getAddress());
            ps.setInt(4,owner.getPhoneNumber());
            ps.setInt(5, owner.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("problème de mise à jour d'un propriétaire");;
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement("DELETE FROM owner WHERE id = ?");

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("problème de suppression d'un propriétaire");;
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public Owner findById(Integer id) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM owner WHERE id = ?");

            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                Owner owner = new Owner();

                owner.setId(rs.getInt("Id"));
                owner.setName(rs.getString("Name"));
                owner.setCIN(rs.getString("Cin"));
                owner.setAddress(rs.getString("Address"));
                owner.setPhoneNumber(rs.getInt("PhoneNumber"));

                return owner;
            }

            return null;
        } catch (SQLException e) {
            System.err.println("problème de requête pour trouver un propriétaire");;
            return null;
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(ps);
        }
    }

    @Override
    public List<Owner> findAll() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM owner");
            rs = ps.executeQuery();

            List<Owner> listOwner = new ArrayList<>();

            while (rs.next()) {
                Owner owner = new Owner();

                owner.setId(rs.getInt("Id"));
                owner.setName(rs.getString("Name"));
                owner.setCIN(rs.getString("Cin"));
                owner.setAddress(rs.getString("Address"));
                owner.setPhoneNumber(rs.getInt("PhoneNumber"));

                listOwner.add(owner);
            }

            return listOwner;
        } catch (SQLException e) {
            System.err.println("problème de requête pour sélectionner un propriétaire");;
            return null;
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(ps);
        }
    }

    @Override
    public List<Owner> readFromTextFile(FileReader fileReader) {
        ArrayList<Owner> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(fileReader);
            Owner o = null;
            String readLine = br.readLine();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            while(readLine != null){
                String [] owner  = readLine.split(",");
                o = new Owner();
                o.setId(Integer.valueOf(owner[0].trim()));
                o.setName(owner[1].trim());
                o.setCIN(owner[2].trim());
                o.setAddress(owner[3].trim());
                o.setPhoneNumber(Integer.valueOf(owner[4].trim()));
                list.add(o);
                readLine = br.readLine();

            }
            System.out.println(list);

        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }



    @Override
    public void readFromDatabaseToTextFile() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Owner> listOwner = new ArrayList<>();
        try {
            ps = conn.prepareStatement("SELECT * FROM owner");
            rs = ps.executeQuery();
            FileOutputStream fout = new FileOutputStream("src/main/resources/owners.txt");
            while (rs.next()) {
                Owner owner = new Owner();
                owner.setId(rs.getInt("Id"));
                owner.setName(rs.getString("Name"));
                owner.setCIN(rs.getString("Cin"));
                owner.setAddress(rs.getString("Address"));
                owner.setPhoneNumber(rs.getInt("PhoneNumber"));
                listOwner.add(owner);
            }
            for(Owner owner : listOwner){
                fout.write(owner.toString().getBytes());
                fout.write('\n');
                System.out.println("owner :"+owner.toString());
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.err.println("problème de requête pour sélectionner un propriétaire");;
        }finally {
            DB.closeResultSet(rs);
            DB.closeStatement(ps);
        }
    }

    @Override
    public void readFromStylSheetAndInsertInDatabase(String path) {
        PreparedStatement ps = null;
        try (FileInputStream fis = new FileInputStream(new File(path))) {
            ps = conn.prepareStatement("INSERT INTO owner (Name, CIN, Address, PhoneNumber) " +
                            "SELECT ?, ?, ?, ? FROM owner WHERE NOT EXISTS " +
                            "(SELECT 1 FROM owner WHERE CIN = ?) LIMIT 1",
                    Statement.RETURN_GENERATED_KEYS);

            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            // Iterate over rows
            for (Row row : sheet) {
                // Skip the first row if it contains headers
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Extract data from cells
                Cell nameCell = row.getCell(1);
                Cell cinCell = row.getCell(2);
                Cell addressCell = row.getCell(3);
                Cell phoneNumberCell = row.getCell(4);
                System.out.println(nameCell.getStringCellValue());

                // Set values to the PreparedStatement
                ps.setString(1, nameCell.getStringCellValue());
                ps.setString(2, cinCell.getStringCellValue());
                ps.setString(3, addressCell.getStringCellValue());
                ps.setInt(4, (int) phoneNumberCell.getNumericCellValue());
                ps.setString(5, cinCell.getStringCellValue());

                // Execute the insert statement
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        System.out.println("Inserted owner with ID: " + id);
                    }
                    rs.close();
                } else {
                    System.out.println("Skipped duplicate owner.");
                }
            }

            workbook.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void writingExcelFile(ObservableList<Owner> list ) throws Exception{

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Producers");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Name");
        headerRow.createCell(2).setCellValue("CIN");
        headerRow.createCell(3).setCellValue("Address");
        headerRow.createCell(4).setCellValue("Phone Number");

        // Create data rows
        int rowNum = 1;
        for (Owner owner : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(owner.getId());
            row.createCell(1).setCellValue(owner.getName());
            row.createCell(2).setCellValue(owner.getCIN());
            row.createCell(3).setCellValue(owner.getAddress());
            row.createCell(4).setCellValue(owner.getPhoneNumber());
        }

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to a file
        FileOutputStream  outputStream = null;
        try {
            outputStream = new FileOutputStream( new File("src/main/resources/files/outputData.xlsx"));
            workbook.write(outputStream);
            System.out.println("Data successfully written to Excel file.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
}
