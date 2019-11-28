/****** Скрипт для команды SelectTopNRows из среды SSMS  ******/
SELECT  g.[group_id]
      ,[educ_type_id]
      ,[teacher_id]
	  ,eps.subject_id
	  ,sp.faculty_id
	  ,count(gs.student_id) колвоСтудентов
  FROM [atu_univer].[dbo].[univer_group] g 
  join univer_educ_plan_pos eps on eps.educ_plan_pos_id = g.educ_plan_pos_id
  join univer_group_student gs on gs.group_id = g.group_id
  join univer_educ_plan ep on ep.educ_plan_id = eps.educ_plan_id
  JOIN univer_speciality sp on sp.speciality_id = ep.speciality_id
   join univer_academ_calendar_pos acc on acc.educ_plan_id = eps.educ_plan_id and acc.acpos_semester = eps.educ_plan_pos_semestr
   where acc.control_id = 0 and (acc.acpos_date_start < GETDATE() and acc.acpos_date_end >GETDATE())
  group by g.[group_id]
      ,[educ_type_id]
      ,[teacher_id]
	  ,eps.subject_id
	  ,sp.faculty_id
	  order by g.group_id desc
