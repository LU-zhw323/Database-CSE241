select ID from instructor where name='Uno';

select distinct building from department natural join instructor where instructor.salary >= 100000 order by department.building;

select student.name, student.dept_name, instructor.name, instructor.dept_name from student, advisor, instructor where student.id=advisor.s_id and advisor.i_id=instructor.id and student.dept_name='Comp. Sci.' and instructor.dept_name='History';

select T.id, T.name from student T where T.dept_name = 'Statistics' and T.tot_cred < (select MAX(S.tot_cred)/10 from student S where S.dept_name = 'Statistics');

select ROUND(AVG(salary)) from instructor where instructor.dept_name='Philosophy';

select dept_name, COUNT(name) total_advised from (select instructor.dept_name, student.name from student join advisor on student.id=advisor.s_id join instructor on advisor.i_id=instructor.id) group by dept_name order by dept_name;

select year, COUNT(id) enrolled from (select distinct student.id, year from takes, student  where takes.id = student.id) group by year having COUNT(id) > 1000 order by year;
