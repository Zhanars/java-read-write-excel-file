/****** Скрипт для команды SelectTopNRows из среды SSMS  ******/
SELECT top 1 g.[group_id]
      ,[educ_type_id]
      ,[teacher_id]
	  ,eps.subject_id
	  ,eps.faculty_id
	  ,count(gs.student_id) колвоСтудентов
  FROM [atu_univer].[dbo].[univer_group] g 
  join univer_educ_plan_pos eps on eps.educ_plan_pos_id = g.educ_plan_pos_id
  join univer_group_student gs on gs.group_id = g.group_id
  group by g.[group_id]
      ,[educ_type_id]
      ,[teacher_id]
	  ,eps.subject_id
	  ,eps.faculty_id
	  order by g.group_id desc
