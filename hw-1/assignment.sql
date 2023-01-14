-- 2.4.1
------------------------------ b ------------------------------
-- Query
SELECT DISTINCT product.maker
FROM product
JOIN laptop
    ON product.type = 'laptop' AND product.model = laptop.model
WHERE laptop.hd >= 100;

-- Answer: A, B, E, F, G
--  maker
-- -------
--  A
--  B
--  E
--  F
--  G

------------------------------ e ------------------------------
-- Query
WITH only_laptop AS (
    SELECT maker
    FROM product
    WHERE product.type = 'laptop'
), only_pc AS (
    SELECT maker
    FROM product
    WHERE product.type = 'pc'
)

SELECT DISTINCT only_laptop.maker 
FROM only_laptop
LEFT JOIN only_pc
    ON only_laptop.maker = only_pc.maker
WHERE only_pc.maker IS NULL;

-- Answer: F, G
--  maker
-- -------
--  F
--  G

------------------------------ g ------------------------------
-- Query
SELECT DISTINCT
    LEAST(pc_1.model, pc_2.model) AS model_1, 
    GREATEST(pc_1.model, pc_2.model) AS model_2, 
    pc_1.speed AS speed, 
    pc_1.ram AS ram
FROM pc AS pc_1, pc AS pc_2
WHERE 
    pc_1.speed = pc_2.speed 
    AND pc_1.ram = pc_2.ram
    AND pc_1.model != pc_2.model;

-- Answer: Models 1004 and 1012 have the same speed and RAM
-- model_1 | model_2 | speed | ram
-- --------+---------+-------+------
--    1004 |    1012 |  2.80 | 1024

------------------------------ i ------------------------------
-- Query
WITH all_computers AS (
    SELECT model, speed, 'laptop' AS "type"
    FROM laptop

    UNION

    SELECT model, speed, 'pc' AS "type"
    FROM pc
), max_speed AS (
    SELECT model, all_computers.type
    FROM all_computers
    WHERE speed = (SELECT MAX(speed) FROM all_computers)
)

SELECT DISTINCT maker
FROM max_speed
JOIN product
    ON max_speed.model = product.model AND max_speed.type = product.type;

-- Answer: B

--  maker
-- -------
--  B