
drop table if exists parent;
create table parent (	
	id	bigint,
	name text,
	income	float4
) distributed by (id);		
 
drop table if exists child;
create table child (		
	id	 bigint,
	parent_id	 bigint,
	name text,
	age int
) distributed by (parent_id);

drop table if exists mapped_child;
create table mapped_child (		
	id	 bigint,
	parent_id	 bigint,
	name text,
	age int
) distributed by (parent_id);

drop table if exists orphan_child;
create table orphan_child (		
	id	 bigint,
	parent_id	 bigint,
	name text,
	age int
) distributed by (parent_id);

INSERT INTO parent VALUES (1, 'Alice',  1.0);
INSERT INTO parent VALUES (2, 'Bob',  250000.0);
INSERT INTO parent VALUES (4, 'Charlie',  245457.0);
INSERT INTO parent VALUES (5, 'Dana',  200.0);
INSERT INTO parent VALUES (6, 'Elle',  55500.0);

INSERT INTO child VALUES (1,1, 'Marsha', 16);
INSERT INTO child VALUES (2,2, 'Frank', 5);
INSERT INTO child VALUES (3,6, 'Grace', 22);
INSERT INTO child VALUES (66,66, 'Damion', 666);

