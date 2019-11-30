package geneticrasp;

//особь в генетическом алгоритме
public class GeneticPerson implements Comparable //для сортировки
{
    public int group_id; //группа
    public int subject_id; //дисциплина
    public int educ_type_id; //тип дисциплины
    public int teacher_id; //преподаватель
    public int faculty_id; //факультет
    public int studcount; //кол-во студентов
    public int educ_plan_pos_credit; //кол-во студентов

    public int auditor; //аудитория
    public int time; //время
    public int day; //день


    public GeneticPerson(int group_id, int subject_id, int educ_type_id, int teacher_id, int faculty_id, int studcount, int educ_plan_pos_credit)
    {
        this.group_id = group_id;
        this.subject_id = subject_id;
        this.educ_type_id = educ_type_id;
        this.teacher_id = teacher_id;
        this.faculty_id = faculty_id;
        this.studcount = studcount;
        this.educ_plan_pos_credit = educ_plan_pos_credit;
    }

    //копирование особи
    public final GeneticPerson clone()
    {
        GeneticPerson gp = new GeneticPerson(group_id, subject_id, educ_type_id, teacher_id, faculty_id, studcount, educ_plan_pos_credit);
        gp.auditor = auditor;
        gp.time = time;
        gp.day = day;


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
            Integer ndata = this.time + this.day * 100;
            Integer ndata1 = gp.time + gp.day * 100;
            return ndata.compareTo(ndata1);
        }
        else
        {
            throw new IllegalArgumentException("Object is not a Temperature");
        }
    }
}
