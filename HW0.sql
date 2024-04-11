select title,semester,year,building from course natural join section where
dept_name = 'Accounting' order by year, semester, title;