package com.axiomsl.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.axiomsl.pdf.ComparePDFs;

public class MoveFiles {

	public static void moveFiles(String fromDir, String toDir) throws IOException {

		ComparePDFs comparePDFs = new ComparePDFs();

		List<Pair<?, ?>> fileList = new ArrayList<Pair<?, ?>>();

		Path fromDirPath = Paths.get(fromDir);
		File toDirFolder = new File(toDir);

		comparePDFs.getFileNames(fileList, fromDirPath);
		FileUtils.cleanDirectory(toDirFolder);
		System.out.println("Cleaned directory: " + toDir);

		try {

			for (Pair<?, ?> filesInFolder : fileList) {

				File moveFile = new File(java.util.Objects.toString(filesInFolder.getLeft()).trim());

				String fileName = java.util.Objects.toString(filesInFolder.getRight()).trim();

				if (moveFile.renameTo(new File(toDir + fileName)))
					System.out.println("Moved: " + fromDir + fileName + " -> " + toDir + fileName);
				else
					System.out.println("Failed to move: " + fromDir + fileName);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
