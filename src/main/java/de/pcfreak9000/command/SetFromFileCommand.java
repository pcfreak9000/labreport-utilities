/*******************************************************************************
 * Copyright (C) 2020 Roman Borris
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.pcfreak9000.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.DataTablet.DataUsage;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "setf", description = "Sets the content of a data tablet from a file")
public class SetFromFileCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-l",
            "--skipl" }, description = "Skips the specified amount of lines. 0 by default.", defaultValue = "0")
    private int skipLines;
    
    @Option(names = { "-r",
            "--skipr" }, description = "Skips the specified amount of rows. 0 by default.", defaultValue = "0")
    private int skipRows;
    
    @Option(names = { "-L",
            "--countl" }, description = "The amount of lines that will be read. By default, all lines will be read.", defaultValue = "-1")
    private int linesCount;
    
    @Option(names = { "-R",
            "--countr" }, description = "The amount of rows that will be read. By default, all rows will be read.", defaultValue = "-1")
    private int rowsCount;
    
    @Option(names = { "-s",
            "--stat" }, description = "Specify this flag if the data is to be intepreted statistically, e.g. if the data represents something that was measured multiple times", defaultValue = "false")
    private boolean statistical;
    
    @Parameters(paramLabel = "FILE", description = "The file from which the tablet is to be filled", index = "1")
    private Path filepath;
    
    @Parameters(paramLabel = "TABLET_NAME", description = "The tablet that is to be filled", index = "0")
    private String tabletName;
    
    @Override
    public void run() {
        File file = filepath.toFile();
        if (!file.exists()) {//TODO does picocli solve this already?
            System.out.println("File does not exist: '" + file.toString() + "'");
            return;
        }
        List<String[]> entries;
        //TODO maybe automatically find out about the tables contents or something
        try (Reader reader = Files.newBufferedReader(filepath)) {
            CSVParser csvParser = new CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .withIgnoreLeadingWhiteSpace(true).withStrictQuotes(false).build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).withCSVParser(csvParser)
                    .build();
            entries = csvReader.readAll();
        } catch (IOException e) {
            System.out.println("Error while reading the file: " + e);
            return;
        }
        Tablet ta = Main.data.getTablet(tabletName);
        if (!(ta instanceof DataTablet)) {
            System.out.println("Cannot execute: Tablet '" + tabletName + "' is not a data tablet");
            return;
        }
        DataTablet dt = (DataTablet) ta;
        List<String> values = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < (linesCount == -1 ? entries.size() : linesCount); i++) {
            int k = 0;
            for (int j = skipRows; j < skipRows + (rowsCount == -1 ? entries.get(i).length : rowsCount); j++) {
                String cell = entries.get(i)[j];
                if (cell != null) {
                    cell = cell.replace(',', '.');
                    if (!cell.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX)) {
                        System.out.println("Cell is not matching the supported number format: '" + cell + "'");
                        return;
                    } //big oof here:
                    if (k == 0) {
                        values.add(cell);
                        k++;
                    } else if (k == 1) {
                        errors.add(cell);
                    }
                }
            }
        }
        dt.setValues(values.toArray(String[]::new));
        dt.setErrors(errors.toArray(String[]::new));
        if (statistical) {
            dt.setDataUsage(DataUsage.MeanAndStandardDeviation);
        } else {
            dt.setDataUsage(DataUsage.Raw);
        } //Thats not nice but it should work... for now
        dt.setPreferredPropagation(PropagationType.get(statistical));
        System.out.println("Successfully read the file '" + file.toString() + "'");
    }
}
