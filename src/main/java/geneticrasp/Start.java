package geneticrasp;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Start {
    //критерии
    private GeneticRooms[] auditors;
    private String[] times;
    private String[] days;
    private int[] teachers;

    private String[] columns = {"Group_id", "Auditor_id", "day", "time"};
    private float minFitn = -1; //лучшее здоровье

    private GeneticPerson[] persons; //расписание

    private Random rand;

    private int time = 0;
    private int timeMax = 10000;

    public Start(GeneticPerson[] groups, int[] teachers, String[] timesfromuniver, GeneticRooms[] auditorsfromuniver)throws SQLException {
        this.times = timesfromuniver;
        this.days = new String []{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        this.persons = groups;
        this.teachers = teachers;
        this.auditors = auditorsfromuniver;


        rand = new Random();
        for (int j = 0; j < persons.length; j++){
            //заполняем случайными значениями
            persons[j].auditor = getauditorforgroup(j);
            persons[j].time = rand.nextInt(times.length);
            persons[j].day = rand.nextInt(days.length);
        }

        //вызываем функцию составления расписания, которая выбирает подходящее время и аудитории для занятий из расписания
        Thread myThread = new Thread() //Создаем новый объект потока (Thread)
        {
            public void run() {
                doColor();
            }
        };

        myThread.start(); //запускаем поток

        System.out.println("finished");
        Excelwriter();
        System.exit(0);
        // doColor();
    }


    //генетический алгоритм
    private void doColor() {
        final int PERSONS = 200; //количество особей

        GeneticPerson[] answer = null; //ответ, готовое расписание


        ArrayList<GeneticPerson[]> personsList = new ArrayList<GeneticPerson[]>(); //список особей

        minFitn = -1;


        //заполняем случайными значениями первую популяцию особей
        for (int i = 0; i < PERSONS; i++) {
            GeneticPerson[] pers = new GeneticPerson[persons.length];
            for (int j = 0; j < persons.length; j++) {
                //неизменные критерии берем из стартовой заготовки расписания
                pers[j] = new GeneticPerson(
                        persons[j].group_id,
                        persons[j].subject_id,
                        persons[j].educ_type_id,
                        persons[j].teacher_id,
                        persons[j].faculty_id,
                        persons[j].studcount
                );

                //остальные критерии генерируем случайным образом
                pers[j].auditor = getauditorforgroup(j);
                pers[j].time = rand.nextInt(times.length);
                pers[j].day = rand.nextInt(days.length);
            }

            personsList.add(pers);
        }


        for (time = 0; time < timeMax; time++) //количество итераций, если решение не будет найдено
        {
            answer = null;
            int indMin = -1; //номер особи с лучшим здоровьем

            ArrayList<Float> personFitness = new ArrayList<Float>(); //здоровье особей

            //считаем здоровье особей
            for (int i = 0; i < PERSONS; i++) {
                personFitness.add(fitness(personsList.get(i)));

                //ищем минимальное здоровье у популяции
                if (indMin == -1 || personFitness.get(i).compareTo(personFitness.get(indMin)) < 0) {
                    indMin = i;
                    answer = personsList.get(i);
                }

                //минимальное здоровье за все время
                if (minFitn > personFitness.get(i) || minFitn == -1) {
                    minFitn = personFitness.get(i);
                }

                //если есть особь с идеальным здоровьем, заканчиваем
                if (personFitness.get(i).equals(0)) {
                    break;
                }

            }


            //если есть особь с идеальным здоровьем, заканчиваем
            if (personFitness.get(indMin).equals(0)) {
                break;
            }


            //размножение
            ArrayList<GeneticPerson[]> newPersonsList = new ArrayList<GeneticPerson[]>(); //список новых особей
            for (int i = 0; i < PERSONS / 2; i++) {
                //выбираем два случайных родителя
                GeneticPerson[] par1 = personsList.get(rand.nextInt(PERSONS));
                GeneticPerson[] par2 = personsList.get(rand.nextInt(PERSONS));

                //создаем ребенка
                GeneticPerson[] child = new GeneticPerson[persons.length];
                //заполняем его гены
                for (int j = 0; j < persons.length; j++) {
                    //случайно берем ген от одного из родителей
                    if (rand.nextInt(2) == 0) {
                        child[j] = par1[j].clone();
                    } else {
                        child[j] = par2[j].clone();
                    }

                    //мутация
                    if (rand.nextInt(5) == 0) {
                        child[j].auditor = getauditorforgroup(j);
                        child[j].time = rand.nextInt(times.length);
                        child[j].day = rand.nextInt(days.length);
                    }
                }

                //добавляем к нововй популяции
                newPersonsList.add(child);

            }

            //отбор, убиваем слабые особи
            for (int i = 0; i < PERSONS / 2; i++) {
                //ищем самую слабую особь
                float maxp = -1;
                int ind = -1;
                for (int j = 0; j < personsList.size(); j++) {
                    float p = personFitness.get(j);
                    if (maxp == -1 || maxp < p) {
                        maxp = p;
                        ind = j;
                    }
                }

                //удаялем слабую особь
                personsList.remove(ind);
                personFitness.remove(ind);
            }

            //объединяем списки
            for (GeneticPerson[] el : newPersonsList) {
                personsList.add(el);
            }

            //если нашли ответ, заносим его в исходный шаблон расписания
            if (answer != null) {
                for (int i = 0; i < persons.length; i++) {
                    persons[i].auditor = answer[i].auditor;
                    persons[i].time = answer[i].time;
                    persons[i].day = answer[i].day;
                }
            }
        }


    }

    //функция определяет здоровье особи
    //personColors - генотип
    private float fitness(GeneticPerson[] personColors) {
        float result = 0; //начальное здоровье

        //проходим по всем вершинам графа
        for (int i = 0; i < personColors.length; i++) {

            //проверяем всех соседей
            for (int j = 0; j < personColors.length; j++) {
                if (i == j) {
                    continue;
                }

                if (personColors[i].time == personColors[j].time && personColors[i].day == personColors[j].day && (personColors[i].teacher_id == personColors[j].teacher_id || personColors[i].auditor == personColors[j].auditor || personColors[i].group_id == personColors[j].group_id)) {

                    //если совпадают
                    if (personColors[i].teacher_id == personColors[j].teacher_id) {
                        result += 100; //уменьшаем здоровье
                    }

                    if (personColors[i].group_id == personColors[j].group_id) {
                        result += 100; //уменьшаем здоровье
                    }

                    if (personColors[i].auditor == personColors[j].auditor) {
                        result += 100; //уменьшаем здоровье
                    }
                }
            }
        }

        //создаем копию массива
        GeneticPerson[] personColorsSortTime = (GeneticPerson[]) personColors.clone();
        Arrays.sort(personColorsSortTime); //сортируем по возрастанию времени

        //проверка окон у преподавателей
        for (int i = 0; i < teachers.length; i++) {
            int state = 0;
            int time = -1;
            int day = -1;
            //проходим по всем занятиям
            for (int j = 0; j < personColorsSortTime.length; j++) {
                //сбрасываем значения флагов, если первый день или новый день
                if (day == -1 || personColorsSortTime[j].day != day) {
                    day = personColorsSortTime[j].day;
                    state = 0;
                    time = -1;
                }

                //если нужный преподаватель
                if (personColorsSortTime[j].teacher_id == teachers[i]) {
                    //еще не было пары
                    if (state == 0) {
                        state = 1;
                    }

                    //была пара и после было окно
                    if (state == 2 || state == 1 && time != -1 && time != personColorsSortTime[j].time - 1) {
                        result++;
                        state = 1;
                    }

                    time = personColorsSortTime[j].time;
                } else {
                    //уже была пара
                    if (state == 1) {
                        state = 2;
                    }
                }
            }
        }


/*        //проверка окон у групп
        for (int i = 0; i < group_id.length; i++) {
            int state = 0;
            int time = -1;
            int day = -1;
            for (int j = 0; j < personColorsSortTime.length; j++) {
                //сбрасываем значения флагов, если первый день или новый день
                if (day == -1 || personColorsSortTime[j].day != day) {
                    day = personColorsSortTime[j].day;
                    state = 0;
                    time = -1;
                }

                //если нужная группа
                if (personColorsSortTime[j].group == i) {
                    //еще не было пары
                    if (state == 0) {
                        state = 1;
                    }

                    //пара была и было окно
                    if (state == 2 || state == 1 && time != -1 && time != personColorsSortTime[j].time - 1) {
                        result++;
                        state = 1;
                    }

                    time = personColorsSortTime[j].time;
                } else {
                    //уже была пара
                    if (state == 1) {
                        state = 2;
                    }
                }
            }
        }*/


        //разные аудитории у групп
        for (int i = 0; i < auditors.length; i++) {
            int state = 0;
            int time = -1;
            int day = -1;
            for (int j = 0; j < personColorsSortTime.length; j++) {
                if (day == -1 || personColorsSortTime[j].day != day) {
                    day = personColorsSortTime[j].day;
                    state = 0;
                    time = -1;
                }

                if (personColorsSortTime[j].auditor == i) {
                    if (state == 0) {
                        state = 1;
                    }


                    if (state == 2 || state == 1 && time != -1 && time != personColorsSortTime[j].time - 1) {
                        result++;
                        state = 1;
                    }

                    time = personColorsSortTime[j].time;
                } else {
                    if (state == 1) {
                        state = 2;
                    }
                }
            }
        }

        /*//одинакого среднее количество занятий для группы
        for (int i = 0; i < groups.length; i++) {
            //создаем массив с количеством пар по дням
            int[] lessonsInDay = new int[days.length];
            for (int j = 0; j < lessonsInDay.length; j++) {
                lessonsInDay[j] = 0;
            }

            //считаем пары
            for (int j = 0; j < personColorsSortTime.length; j++) {
                lessonsInDay[personColorsSortTime[j].day]++;
            }

            //ищем среднее количество
            int avg = 0;
            for (int j = 0; j < lessonsInDay.length; j++) {
                avg += lessonsInDay[j];
            }

            avg = avg / lessonsInDay.length;

            //если среднее не выполняется, то ухудшаем здоровье
            for (int j = 0; j < lessonsInDay.length; j++) {
                if (avg != lessonsInDay[j]) {
                    result++;
                }
            }

        }*/


        return result;
    }
    public int getauditorforgroup(int id){
        int result;
        rand = new Random();
        /*do {
            result = rand.nextInt(auditors.length);
        }while((persons[id].educ_type_id != auditors[result].audience_type_id) ||
                (persons[id].faculty_id != auditors[result].faculty_id) ||
                (persons[id].studcount > auditors[result].audience_size));*/

        result = rand.nextInt(auditors.length);
        return result;
    }
    public void Excelwriter(){
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook();     // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances for various things like DataFormat,
           Hyperlink, RichTextString etc in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Schedule");

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Creating cells
        for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Other rows and cells with employees data
        int rowNum = 1;

        for (int j = 0; j < persons.length; j++) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(persons[j].group_id);
            row.createCell(1).setCellValue(auditors[persons[j].auditor].audience_id);
            row.createCell(2).setCellValue(days[persons[j].day]);
            row.createCell(3).setCellValue(times[persons[j].day]);

        }
        // Resize all columns to fit the content size
        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream("poi-generated-file.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

