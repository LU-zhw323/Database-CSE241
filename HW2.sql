select x.building, x.occurrences from (select building, count(building) occurrences from (select building from section group by course_id, building,room_number having count(course_id) >= 3) group by building) x where x.occurrences=(select max(occurrences) from (select building, count(building) occurrences from (select building from section group by course_id, building,room_number having count(course_id) >= 3) group by building)); 

select instructor.id, count(*) advisees from instructor, advisor, student where instructor.id=advisor.i_id and advisor.s_id=student.id group by instructor.id having count(*) >=40;

select round(avg(salary)-sum(salary)/count(*)-1,5) from instructor;
select round(avg(salary)-sum(salary)/(count(*)-1),5) from instructor;
select * from instructor where salary is null;

select course_id,title from course where course_id not in (select prereq_id from prereq) and dept_name='Math' order by course_id;

select distinct course_id, title from course natural left outer join prereq where prereq_id is null and title like '%of%' order by course_id desc;




create table cssection(course_id char(5) not null, sec_id char(5) not null, semester varchar(10) not null, year numeric(4,0), building varchar(20) not null, room_number char(5) not null, time_slot_id char(5) not null, primary key(course_id,sec_id,semester,year));
create table csstudent(id char(5) not null, name varchar(20) not null, dept_name varchar(20) not null, tot_cred int not null, primary key(id));
create table cstakes(id char(5) not null, course_id char(5) not null, sec_id char(5) not null, semester varchar(10) not null, year numeric(4,0), grade varchar(2), primary key(id,course_id,sec_id,semester,year), foreign key(id) references csstudent, foreign key(course_id,sec_id,semester,year) references cssection);

select * from cssection;
select * from csstudent;
select * from cstakes;

insert into cssection select course_id, sec_id, semester, year, building, room_number, time_slot_id from section natural join course where course.dept_name='Comp. Sci.';
insert into csstudent select * from student where student.dept_name='Comp. Sci.';
insert into cstakes select id, course_id, sec_id, semester, year, grade from takes natural join student natural join section natural join course where dept_name='Comp. Sci.';

insert into csstudent values ('2','Duo','Comp. Sci.', 60);

insert into cstakes (id, course_id, sec_id, semester, year, grade) select id, course_id, sec_id, semester, year, 'A' from cssection, csstudent where  year=2006 and course_id between 200 and 400 and sec_id=1 and id=2;
