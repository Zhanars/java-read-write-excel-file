/****** Скрипт для команды SelectTopNRows из среды SSMS  ******/
SELECT TOP (1000) [schedule_time_id]
      ,[schedule_time_begin]
      ,[schedule_time_end]
  FROM [atu_univer].[dbo].[univer_schedule_time] where status = 1 and schedule_time_type_id = 1
  order by schedule_time_begin