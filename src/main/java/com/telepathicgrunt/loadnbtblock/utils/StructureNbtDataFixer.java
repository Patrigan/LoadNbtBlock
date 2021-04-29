package com.telepathicgrunt.loadnbtblock.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.DefaultTypeReferences;

import java.io.*;
import java.util.List;


public class StructureNbtDataFixer {

    //source: https://stackoverflow.com/a/14676464
    public static void setAllNbtFilesToList(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.isFile() && file.getName().contains(".nbt")) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    setAllNbtFilesToList(file.getAbsolutePath(), files);
                }
            }
        }
    }

    public static void updateAllNbtFiles(String directoryName, List<File> files) throws IOException {
        setAllNbtFilesToList(directoryName, files);
        for(File file : files){
            InputStream inputStream = new FileInputStream(file);

            File resultingFile = new File(directoryName+"//"+file.getAbsolutePath().split("resources\\\\")[1]);
            resultingFile.getParentFile().mkdirs();
            OutputStream outputStream = new FileOutputStream(resultingFile);

            CompoundNBT newNBT = updateNbtCompound(inputStream);
            CompressedStreamTools.writeCompressed(newNBT, outputStream);
        }
    }

    public static CompoundNBT updateNbtCompound(InputStream structureInputStream) throws IOException {
        CompoundNBT compoundTag = CompressedStreamTools.readCompressed(structureInputStream);
        return NBTUtil.update(DataFixesManager.getDataFixer(), DefaultTypeReferences.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion"), compoundTag.getInt("DataVersion"));
    }
}
