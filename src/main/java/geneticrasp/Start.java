package geneticrasp;

import java.util.*;

public class Start {
    //критерии
    private String[] groups;
    private String[] auditors;
    private String[] disciplins;
    private String[] prepods;
    private String[] times;
    private String[] types;
    private String[] days;
    private float minFitn = -1; //лучшее здоровье

    private GeneticPerson[] persons; //расписание

    private Random rand;

    private int time = 0;
    private int timeMax = 10000;
    public static void main(String[] args){

    }
    public void Start(String[] groups, String[] auditors, String[] disciplins, String[] prepods, String[] times, String[] types, String[] days) {

        this.groups = groups;
        this.auditors = auditors;
        this.disciplins = disciplins;
        this.prepods = prepods;
        this.times = times;
        this.types = types;
        this.days = days;

        rand = new Random();

        //количество занятий
        int num = groups.length * disciplins.length * types.length;

        //создаем расписание
        persons = new GeneticPerson[num];
        int n = 0;

        //int radius = panel1.Height / 2 - 40;   //радиус круга графа
        double ang = Math.PI * 2 / num;

        //создаем занятия
        for (int i = 0; i < groups.length; i++) {
            for (int j = 0; j < disciplins.length; j++) {
                for (int k = 0; k < types.length; k++) {
                    persons[n] = new GeneticPerson(i, j, k);

                    persons[n].x = (float) Math.cos(n * ang);
                    persons[n].y = (float) Math.sin(n * ang);

                    //заполняем случайными значениями
                    persons[n].auditor = rand.nextInt(auditors.length);
                    persons[n].prepod = rand.nextInt(prepods.length);
                    persons[n].time = rand.nextInt(times.length);
                    persons[n].day = rand.nextInt(days.length);

                    n++;

                }
            }
        }

        //вызываем функцию составления расписания, которая выбирает подходящее время и аудитории для занятий из расписания
        Thread myThread = new Thread() //Создаем новый объект потока (Thread)
        {
            public void run() {
                doColor();
            }
        };

        myThread.start(); //запускаем поток

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
                pers[j] = new GeneticPerson(persons[j].group, persons[j].discipl, persons[j].type);
                pers[j].prepod = persons[j].prepod;

                //остальные критерии генерируем случайным образом
                pers[j].auditor = rand.nextInt(auditors.length);
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
                        child[j].auditor = rand.nextInt(auditors.length);
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

                if (personColors[i].time == personColors[j].time && personColors[i].day == personColors[j].day && (personColors[i].prepod == personColors[j].prepod || personColors[i].auditor == personColors[j].auditor || personColors[i].group == personColors[j].group)) {

                    //если совпадают
                    if (personColors[i].prepod == personColors[j].prepod) {
                        result += 100; //уменьшаем здоровье
                    }

                    if (personColors[i].group == personColors[j].group) {
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
        for (int i = 0; i < prepods.length; i++) {
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
                if (personColorsSortTime[j].prepod == i) {
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


        //проверка окон у групп
        for (int i = 0; i < groups.length; i++) {
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
        }


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

        //одинакого среднее количество занятий для группы
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

        }


        return result;
    }
}

