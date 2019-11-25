package geneticrasp;

//особь в генетическом алгоритме
public class GeneticPerson implements Comparable //для сортировки
{
    public int group; //группа
    public int discipl; //дисциплина
    public int type; //тип дисциплины

    public int auditor; //аудитория
    public int prepod; //преподаватель
    public int time; //время
    public int day; //день

    public float x; //координаты для вывода графа
    public float y;

    public GeneticPerson(int group, int discipl, int type)
    {
        this.group = group;
        this.discipl = discipl;
        this.type = type;
    }

    //копирование особи
    public final GeneticPerson clone()
    {
        GeneticPerson gp = new GeneticPerson(group, discipl, type);
        gp.auditor = auditor;
        gp.prepod = prepod;
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
