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
    private int[][] roomsCount;
    private int[][] groupsCount;
    private int[] teachers;

    private String[] columns = {"group_id", "teachers", "auditor_id", "day", "time"};

    private GeneticPerson[] persons; //расписание

    private Random rand;

    private int timeMax = 1000000000;

    public Start(GeneticPerson[] groups, int[] teachers, String[] timesfromuniver, GeneticRooms[] auditorsfromuniver, int[][] roomsCount, int[][] groupsCount){
        this.times = timesfromuniver;
        this.days = new String []{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        this.persons = groups;
        this.roomsCount = roomsCount;
        this.groupsCount = groupsCount;
        this.teachers = teachers;
        this.auditors = auditorsfromuniver;
        System.out.println(times.length);
        System.out.println(days.length);
        System.out.println(persons.length);
        System.out.println(teachers.length);
        System.out.println(auditors.length);
        rand = new Random();
        for (int j = 0; j < persons.length; j++){
            //заполняем случайными значениямии
            if (persons[j].status != 2) {
                persons[j].audience_id = getauditorforgroup(this.persons[j]);
                persons[j].time_id = rand.nextInt(times.length);
                persons[j].day_of_week_id = rand.nextInt(days.length);
            }
        }


        //вызываем функцию составления расписания, которая выбирает подходящее время и аудитории для занятий из расписания
        Thread myThread = new Thread(){
            @Override
            public void run() {
                doColor();
            }
        };
        myThread.run();
        System.out.println("dsadasd");
    }


    //генетический алгоритм
    private void doColor() {

        GeneticPerson[] answer = null; //ответ, готовое расписание
        final int PERSONS = 200; //количество особей
        ArrayList<GeneticPerson[]> personsList = new ArrayList<GeneticPerson[]>(); //список особей

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
                            persons[j].students_count,
                            persons[j].educ_plan_pos_credit
                    );
                    //остальные критерии генерируем случайным образом
                    pers[j].auditor = getauditorforgroup(pers[j]);
                    pers[j].time = rand.nextInt(times.length);
                    pers[j].day = rand.nextInt(days.length);

            }
            personsList.add(pers);
        }


        int minFitn = -1; //лучшее здоровье

        while (1 > 0) //количество итераций, если решение не будет найдено
        {
            System.out.println("working");
            if (rand.nextInt(5) == 0) {
                System.out.print(String.format("\033[2J"));
            }
            int indMin = -1; //номер особи с лучшим здоровьем

            ArrayList<Integer> personFitness = new ArrayList<Integer>(); //здоровье особей

            //считаем здоровье особей
            for (int i = 0; i < PERSONS; i++) {
                personFitness.add(fitness(personsList.get(i)));

                //ищем минимальное здоровье у популяции
                if (indMin == -1 || personFitness.get(i) < personFitness.get(indMin)) {
                    indMin = i;
                }

                //минимальное здоровье за все время
                if (minFitn > personFitness.get(i) || minFitn == -1) {
                    minFitn = personFitness.get(i);
                }

                //если есть особь с идеальным здоровьем, заканчиваем
                if (personFitness.get(i) == 0) {
                    answer = personsList.get(i);
                    break;
                }
            }
            System.out.println(minFitn);
            //если нашли ответ, заносим его в исходный шаблон расписания
            if (answer != null ) {
                for (int i = 0; i < persons.length; i++) {
                    persons[i].auditor = answer[i].auditor;
                    persons[i].time = answer[i].time;
                    persons[i].day = answer[i].day;
                }
                System.out.println("persons = answer");
                Excelwriter();
                System.out.println("finished");
                System.exit(0);
                break;
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

            System.out.println("killing");

            //размножение
            ArrayList<GeneticPerson[]> newPersonsList = new ArrayList<GeneticPerson[]>(); //список новых особей
            for (int i = 0; i < PERSONS / 2; i++) {
                //выбираем два случайных родителя
                GeneticPerson[] par1 = personsList.get(rand.nextInt(PERSONS / 2));
                GeneticPerson[] par2 = personsList.get(rand.nextInt(PERSONS / 2));

                //создаем ребенка
                GeneticPerson[] child = new GeneticPerson[persons.length];
                //заполняем его гены
                if (rand.nextInt(2) == 0) {
                    if (rand.nextInt(2) == 0){
                        child = makeNewPop(par1);
                    } else {
                        child = makeNewPop(par2);
                    }
                } else {
                    for (int j = 0; j < persons.length; j++) {
                        //случайно берем ген от одного из родителей
                        if (rand.nextInt(2) == 0) {
                            child[j] = par1[j].clone();
                        } else {
                            child[j] = par2[j].clone();
                        }
                        //мутация
                        if (rand.nextInt(5) == 0) {
                            child[j].auditor = getauditorforgroup(child[j]);
                            child[j].time = rand.nextInt(times.length);
                            child[j].day = rand.nextInt(days.length);
                        }
                    }
                }

                //добавляем к нововй популяции
                newPersonsList.add(child);
            }
            System.out.println("new pop");

            //объединяем списки
            for (GeneticPerson[] el : newPersonsList) {
                personsList.add(el);
            }
        }
    }


    //функция определяет здоровье особи
    //personColors - генотип
    private int fitness(GeneticPerson[] personColors) {
        int result = 0; //начальное здоровье
        //проходим по всем вершинам графа
        for (int i = 0; i < personColors.length; i++) {
            //проверяем всех соседей
            for (int j = 0; j < personColors.length; j++) {
                if (i == j) {
                    continue;
                }

                if (personColors[i].time == personColors[j].time && personColors[i].day == personColors[j].day) {
                    //если совпадают
                    if (personColors[i].teacher_id == personColors[j].teacher_id) {
                        //result += 10; //уменьшаем здоровье
                    }
                    //если совпадают
                    if (personColors[i].auditor == personColors[j].auditor) {
                        result += 10; //уменьшаем здоровье
                    }
                    //если совпадают
                    if (personColors[i].group_id == personColors[j].group_id) {
                        //result += 10; //уменьшаем здоровье
                    }
                }
            }
        }

        //создаем копию массива
        GeneticPerson[] personColorsSortTime = (GeneticPerson[]) personColors.clone();
        Arrays.sort(personColorsSortTime); //сортируем по возрастанию времени*/

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
                        //result++;
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


        /*//разные аудитории у групп
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
        }*/

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

        //System.out.println(result);
        return result;
    }

    private int getauditorforgroup(GeneticPerson geneticPerson) {
        int result, count = 0;
        Map<Integer, Integer> arr = new HashMap<>();
        rand = new Random();
        for(int i = 0; i < auditors.length; i++){
            if (geneticPerson.status == 1) {
                if ((getaudiencetypeidchild(geneticPerson) == auditors[i].audience_type_id) &&
                        (geneticPerson.faculty_id == auditors[i].faculty_id) &&
                        (geneticPerson.students_count <= auditors[i].audience_size)) {
                    arr.put(count, i);
                    count++;
                }
            } else {
                if ((getaudiencetypeidchild(geneticPerson) == auditors[i].audience_type_id) &&
                        (geneticPerson.chair_id == auditors[i].chair_id) &&
                        (geneticPerson.students_count <= auditors[i].audience_size)) {
                    arr.put(count, i);
                    count++;
                }
            }
        }
        result = rand.nextInt(count);
        return arr.get(result);
    }
    private int getAuditorCount(GeneticPerson geneticPerson) {
        int count = 0;
        for(int i = 0; i < auditors.length; i++){
            if ((getaudiencetypeidchild(geneticPerson) == auditors[i].audience_type_id) &&
                    (geneticPerson.faculty_id == auditors[i].faculty_id) &&
                    (geneticPerson.students_count <= auditors[i].audience_size)){
                count++;
            }
        }
        return count;
    }

    private int getaudiencetypeidchild(GeneticPerson geneticPerson) {
        int result;
        switch (geneticPerson.educ_type_id){
            case 1:
                result = 2;
                break;
            case 2:
                result = 3;
                break;
            case 3:
                result = 4;
                break;
            case 7:
                result = 3;
                break;
            default:
                result = 2;
                break;
        }
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
            row.createCell(1).setCellValue(persons[j].teacher_id);
            row.createCell(2).setCellValue(persons[j].auditor);
            row.createCell(3).setCellValue(persons[j].day);
            row.createCell(4).setCellValue(persons[j].time);

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
    public class MyThread extends Thread {
        public void run() {
            doColor();
        }
    }
    private GeneticPerson[] makeNewPop(GeneticPerson[] personColors) {
        GeneticPerson[] result; //начальное здоровье
        //проходим по всем вершинам графа
        for (int i = 0; i < personColors.length; i++) {
            //проверяем всех соседей
            for (int j = i + 1; j < personColors.length; j++) {
                if (personColors[i].time == personColors[j].time && personColors[i].day == personColors[j].day) {
                    //если совпадают
                    if (personColors[i].auditor == personColors[j].auditor) {
                        int result1;
                        switch (personColors[j].educ_type_id){
                            case 1:
                                result1 = 2;
                                break;
                            case 2:
                                result1 = 3;
                                break;
                            case 3:
                                result1 = 4;
                                break;
                            case 7:
                                result1 = 3;
                                break;
                            default:
                                result1 = 2;
                                break;
                        }
                        if ((getAuditorCount(personColors[j]) * times.length * days.length / 2) < (groupsCount[personColors[j].faculty_id][result1])){
                            personColors[j].time = rand.nextInt(times.length);
                            personColors[j].day = rand.nextInt(days.length);
                        } else {
                            personColors[j].auditor = getauditorforgroup(personColors[j]);
                        }
                    }

                    if (personColors[i].teacher_id == personColors[j].teacher_id){
                        personColors[j].time = rand.nextInt(times.length);
                        personColors[j].day = rand.nextInt(days.length);
                    }
                }
            }
        }
        result = personColors.clone();
        return result;
    }
}


