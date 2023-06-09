package com.example.javafx_project.services;

import com.example.javafx_project.dao.OwnerDao;
import com.example.javafx_project.dao.impl.OwnerDaoImp;
import com.example.javafx_project.entities.Owner;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileReader;
import java.util.List;

public class OwnerService {
    private OwnerDao ownerDao =new OwnerDaoImp();

    public List<Owner> findAll() {
        return ownerDao.findAll();
    }

    public void save(Owner owner) {

        ownerDao.insert(owner);

    }
    public void update(Owner owner) {
        ownerDao.update(owner);
    }
    public void remove(Owner owner) {
        ownerDao.deleteById(owner.getId());
    }

    public List<Owner> readFromTextFile(FileReader fileReader){
        return ownerDao.readFromTextFile(fileReader);
    }

    public void readFromDatabaseToTextFile(){
        ownerDao.readFromDatabaseToTextFile();
    }

    public void writingExcelFile(ObservableList<Owner> list) throws Exception{
        ownerDao.writingExcelFile(list);
    }

    public void readFromStyleSheetAndInsertInDatabase(String path){
        ownerDao.readFromStylSheetAndInsertInDatabase(path);
    }
}
