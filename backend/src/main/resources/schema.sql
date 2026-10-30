-- ========================================
-- 可视化放学系统 - MySQL 数据库初始化脚本 (bcrypt 版)
-- 数据库: school_dismissal
-- 注意: 密码字段长度改为 VARCHAR(255) 以容纳 bcrypt 哈希
-- ========================================

CREATE DATABASE IF NOT EXISTS school_dismissal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE school_dismissal;

-- ========================================
-- 1. 用户表
-- ========================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码（bcrypt 加密）',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  role VARCHAR(20) NOT NULL DEFAULT 'teacher' COMMENT '角色：admin/teacher',
  phone VARCHAR(20) COMMENT '电话',
  email VARCHAR(100) COMMENT '邮箱',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 班级表
-- ========================================
DROP TABLE IF EXISTS classes;
CREATE TABLE classes (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '班级名称',
  grade VARCHAR(20) NOT NULL COMMENT '年级',
  head_teacher_id INT COMMENT '班主任ID',
  dismissal_time VARCHAR(10) COMMENT '放学时间',
  school_gate VARCHAR(20) DEFAULT '正门' COMMENT '放学校门',
  status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (head_teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ========================================
-- 3. 学生表
-- ========================================
DROP TABLE IF EXISTS students;
CREATE TABLE students (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '学生姓名',
  student_no VARCHAR(50) UNIQUE COMMENT '学号',
  class_id INT NOT NULL COMMENT '班级ID',
  gender VARCHAR(10) COMMENT '性别：男/女',
  parent_name VARCHAR(50) COMMENT '家长姓名',
  parent_phone VARCHAR(20) COMMENT '家长电话',
  status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (class_id) REFERENCES classes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ========================================
-- 4. 放学记录表
-- ========================================
DROP TABLE IF EXISTS dismissal_records;
CREATE TABLE dismissal_records (
  id INT PRIMARY KEY AUTO_INCREMENT,
  class_id INT NOT NULL COMMENT '班级ID',
  student_id INT COMMENT '学生ID（可选，单生放学）',
  dismissal_time DATETIME NOT NULL COMMENT '放学时间',
  gate VARCHAR(20) COMMENT '校门位置',
  status VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
  operator_id INT COMMENT '操作人ID',
  remark VARCHAR(255) COMMENT '备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (class_id) REFERENCES classes(id),
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (operator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='放学记录表';

-- ========================================
-- 5. 通知公告表
-- ========================================
DROP TABLE IF EXISTS notices;
CREATE TABLE notices (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL COMMENT '通知标题',
  content TEXT NOT NULL COMMENT '通知内容',
  type VARCHAR(20) DEFAULT 'general' COMMENT '类型：general/important',
  target_audience VARCHAR(50) DEFAULT 'all' COMMENT '目标受众',
  author_id INT COMMENT '作者ID',
  is_published TINYINT(1) DEFAULT 1 COMMENT '是否发布：0/1',
  read_count INT DEFAULT 0 COMMENT '阅读次数',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ========================================
-- 6. 留堂记录表
-- ========================================
DROP TABLE IF EXISTS detention_records;
CREATE TABLE detention_records (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL COMMENT '学生ID',
  reason VARCHAR(255) NOT NULL COMMENT '留堂原因',
  detention_date DATE NOT NULL COMMENT '留堂日期',
  operator_id INT COMMENT '操作人ID',
  notified TINYINT(1) DEFAULT 0 COMMENT '是否已通知家长',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (operator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留堂记录表';

-- ========================================
-- 提示: 初始化示例数据请使用 Node.js 脚本:
--   npm run init-db
-- 该脚本会自动使用 bcrypt 加密默认密码
-- ========================================
SELECT '数据库表结构已创建（6 张表）' AS message;
SELECT '请运行 npm run init-db 初始化示例数据（bcrypt 加密）' AS tip;