package geneticrasp;

//особь в генетическом алгоритме
public class GeneticPerson implements Comparable //для сортировки
{
    public Integer[] group_id; //группа
    public Integer[] subject_id; //дисциплина
    public Integer[] teacher_id; //преподаватель
    public int educ_type_id; //тип дисциплины
    public int faculty_id; //факультет
    public int chair_id; //факультет
    public int students_count; //кол-во студентов
    public int hours_educ; //кол-во часов
    public int audience_id; //аудитория
    public int time_id; //время
    public int day_of_week_id; //день
    public int status; //постоянная
    public Integer[] students; //преподаватель


    public GeneticPerson(Integer[] group_id, Integer[] subject_id, Integer[] teacher_id, int educ_type_id, int faculty_id,
                         int chair_id, int students_count, int hours_educ, int audience_id, int time_id, int day_of_week_id, int status, Integer[] students)
    {
        this.group_id = group_id;
        this.subject_id = subject_id;
        this.teacher_id = teacher_id;
        this.educ_type_id = educ_type_id;
        this.faculty_id = faculty_id;
        this.chair_id = chair_id;
        this.students_count = students_count;
        this.hours_educ = hours_educ;
        this.audience_id = audience_id;
        this.time_id = time_id;
        this.day_of_week_id = day_of_week_id;
        this.status = status;
        this.students = students;
    }

    //копирование особи
    public final GeneticPerson clone()
    {
        GeneticPerson gp = new GeneticPerson(group_id, subject_id, teacher_id, educ_type_id,  faculty_id,
                chair_id, students_count, hours_educ, audience_id, time_id, day_of_week_id, status, students);
        gp.audience_id = audience_id;
        gp.time_id = time_id;
        gp.day_of_week_id = day_of_week_id;


        return gp;
    }

    @Override
    //сравнение, для сортировки по времени
    public final int compareTo(Object obj)
    {
        if (obj == null)
        {
            return 1;
        }

        GeneticPerson gp = obj instanceof GeneticPerson ? (GeneticPerson)obj : null;
        if (gp != null) {
            Integer ndata = this.time_id + this.day_of_week_id * 100;
            Integer ndata1 = gp.time_id + gp.day_of_week_id * 100;
            return ndata.compareTo(ndata1);
        }
        else
        {
            throw new IllegalArgumentException("Object is not a Temperature");
        }
    }
}
