package geneticrasp;


public class GeneticRooms //
{
    public int audience_id; //id аудитории
    public int faculty_id; //id факультет
    public int building_id; //id здания
    public int audience_type_id; //тип аудитории
    public int audience_floor; //этаж аудитории
    public int audience_size; //вмещаемость
    public String audience_number_ru; //номер аудитории



    public GeneticRooms(int audience_id, int faculty_id, int building_id, int audience_type_id, int audience_floor, int audience_size, String audience_number_ru)
    {
        this.audience_id = audience_id;
        this.faculty_id = faculty_id;
        this.building_id = building_id;
        this.audience_type_id = audience_type_id;
        this.audience_floor = audience_floor;
        this.audience_size = audience_size;
        this.audience_number_ru = audience_number_ru;
    }
}
