# labreport-utilities
labreport-utilities is a console java program (for now only) intended to automate error propagation for lab reports (or other stuff).
This means reading a function and some data, doing the error propagation, and outputting the results. 
Supporting functionality, e.g. a way to get the TeX commands from the function or the error propagation is also included.  

## Running the program
A Java JRE version 8 or greater is required. To use the program, start the terminal of you preference and navigate to the program files location. Then, type ``java -jar <program file name>``. 
Using this method, the program will start and wait for your input in the terminal you've choosen. ``<program file name>`` is most likely something along the lines of ``lu-xxx.jar`` if you've just downloaded it.
You can also read the instructions from another file. To use this method, simply state the path to the file containing the instructions (newline-seperated, [example](https://github.com/pcfreak9000/labreport-utilities/blob/main/test.txt)) as program argument: ``java -jar <program file name> <path to instruction file>``.    

## Some comments on the usage
Data is stored in so called tablets. Currently, there are two types of tablets: Function and Data tablets. 
Function tablets store mathematical functions and their arguments, data tablets store data in the form of value-error pairs (errors can be 0 of course). 
Tablets can be created with the ``create`` command. Setting function tablets and single-entry-data-tablets can be done by using the ``sete`` command. 
Values and errors can be mathematical expressions that evaluate to a number, for example ``(2+Pi)^3`` or ``Sqrt(9)/4`` when using the ``sete`` command. 

Reading csv-files can be done by using the ``setf`` command. In that case, only numbers are allowed.
Excel (or LibreOffice or others) support the export of csv-files. Also, when using the ``print``-command on a data tablet, the output can be pasted into a spreadsheet which should recognize the format (or will ask at least about that).

Dedicated explanations of the commands can be found when using the help options on a command, e.g. ``propagate -h``. An overview of all the commands can be found when just typing ``-h``.
[Here is an example](https://github.com/pcfreak9000/labreport-utilities/blob/main/test.txt) on how to use this tool.

## Notes
- Don't use mathematical constants as variables, for example: e or E, I or i, ...
- If a file name or a function or any other argument contains a space, you can put " " around that expression. This way, it gets registered as one argument even though it contains spaces. 
- Don't trust this program 100%. There might be bugs...
- Case sensitivity doesn't exactly work great at the moment

## Third-party librarys
- For evaluating mathematical expressions and doing some symbolic math: [Symja / Matheclipse (Github)](https://github.com/axkr/symja_android_library)
- For the console interface and the commands: [Picocli (Github)](https://github.com/remkop/picocli) / [Picocli (Website)](https://picocli.info/)