select year, semester, dept_name, count£¨*£©as enrollment from takes natural join section natural join course group by rollup(year, semester, dept_name);

select year, month,day, nyse.shares_traded, rank() over (order by shares_traded desc) as mostshares from nyse;

select year, month, day, sum(shares_traded) as shares, sum(num_trades) as trades, sum(dollar_volume) as volume from nyse group by rollup(year, month, day);


group by rollup(a), rollup(b), rollup(c ), rollup(d);