package com.telepathicgrunt.loadnbtblock.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.DataFixTypes;

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

            CompoundTag newNBT = updateNbtCompound(inputStream);
            NbtIo.writeCompressed(newNBT, outputStream);
        }
    }

    public static CompoundTag updateNbtCompound(InputStream structureInputStream) throws IOException {
        CompoundTag compoundTag = NbtIo.readCompressed(structureInputStream);
        return NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion"), compoundTag.getInt("DataVersion"));
    }
}
