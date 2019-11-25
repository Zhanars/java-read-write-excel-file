/****** Скрипт для команды SelectTopNRows из среды SSMS  ******/
SELECT [audience_id]
      ,[faculty_id]
      ,[building_id]
      ,[audience_type_id]
      ,[audience_floor]
      ,[audience_size]
      ,[audience_number_ru]  
  FROM [atu_univer].[dbo].[univer_audience] where status = 1
