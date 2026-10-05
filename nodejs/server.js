const express = require("express");
const mysql = require("mysql2");

const app = express();
const PORT = 3000;

const db = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "",
    database: "student_placement_system"
});

db.connect((err) => {
    if (err) {
        console.log("MySQL connection failed:", err.message);
        return;
    }

    console.log("MySQL connected successfully");
});

app.get("/", (req, res) => {

    const queries = {
        students: `
            SELECT s.name, s.roll_no, d.department_name, s.cgpa
            FROM student s
            JOIN department d
            ON s.department_id = d.department_id
            ORDER BY s.student_id
        `,

        companies: `
            SELECT company_name, industry, location
            FROM company
            ORDER BY company_id
        `,

        drives: `
            SELECT c.company_name, j.job_role, j.job_type,
                   j.minimum_cgpa, j.package_lpa,
                   j.drive_date, j.vacancies
            FROM job_drive j
            JOIN company c
            ON j.company_id = c.company_id
            ORDER BY j.drive_id
        `,

        applications: `
            SELECT s.name AS student_name,
                   c.company_name,
                   j.job_role,
                   j.package_lpa,
                   a.application_date,
                   a.status
            FROM application a
            JOIN student s
            ON a.student_id = s.student_id
            JOIN job_drive j
            ON a.drive_id = j.drive_id
            JOIN company c
            ON j.company_id = c.company_id
            ORDER BY a.application_id
        `,

        interviews: `
            SELECT s.name AS student_name,
                   c.company_name,
                   ir.round_name,
                   ir.round_date,
                   ir.result,
                   ir.remarks
            FROM interview_round ir
            JOIN application a
            ON ir.application_id = a.application_id
            JOIN student s
            ON a.student_id = s.student_id
            JOIN job_drive j
            ON a.drive_id = j.drive_id
            JOIN company c
            ON j.company_id = c.company_id
            ORDER BY ir.round_id
        `,

        placements: `
            SELECT s.name AS student_name,
                   c.company_name,
                   j.job_role,
                   pr.final_status,
                   pr.joining_date,
                   pr.offered_package
            FROM placement_result pr
            JOIN application a
            ON pr.application_id = a.application_id
            JOIN student s
            ON a.student_id = s.student_id
            JOIN job_drive j
            ON a.drive_id = j.drive_id
            JOIN company c
            ON j.company_id = c.company_id
            ORDER BY pr.result_id
        `,

        internships: `
            SELECT s.name AS student_name,
                   c.company_name,
                   i.internship_role,
                   i.start_date,
                   i.end_date,
                   i.stipend,
                   i.status
            FROM internship i
            JOIN student s
            ON i.student_id = s.student_id
            JOIN company c
            ON i.company_id = c.company_id
            ORDER BY i.internship_id
        `
    };

    const keys = Object.keys(queries);
    const results = {};

    function runQuery(index) {

        if (index >= keys.length) {
            showDashboard(res, results);
            return;
        }

        const key = keys[index];

        db.query(queries[key], (err, data) => {

            if (err) {
                return res.send("Database error: " + err.message);
            }

            results[key] = data;
            runQuery(index + 1);
        });
    }

    runQuery(0);
});


function showDashboard(res, data) {

    let html = `
<!DOCTYPE html>
<html>

<head>

<title>Student Placement & Internship Management System</title>

<style>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    font-family: Arial, sans-serif;
    background: #f4f6f8;
    color: #1f2937;
}

.header {
    background: #172554;
    color: white;
    padding: 28px;
    text-align: center;
}

.header h1 {
    margin: 0;
    font-size: 30px;
}

.header p {
    margin-top: 8px;
    color: #dbeafe;
}

.container {
    width: 94%;
    max-width: 1400px;
    margin: 25px auto;
}

.cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 18px;
    margin-bottom: 30px;
}

.card {
    background: white;
    padding: 22px;
    border-radius: 12px;
    box-shadow: 0 3px 12px rgba(0,0,0,0.08);
    text-align: center;
}

.card h2 {
    margin: 0;
    font-size: 32px;
    color: #1d4ed8;
}

.card p {
    margin: 8px 0 0;
    color: #64748b;
}

.section {
    background: white;
    padding: 22px;
    margin-bottom: 25px;
    border-radius: 12px;
    box-shadow: 0 3px 12px rgba(0,0,0,0.08);
}

.section h2 {
    margin-top: 0;
    color: #172554;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 15px;
}

th {
    background: #1d4ed8;
    color: white;
    padding: 12px;
    text-align: left;
}

td {
    padding: 11px;
    border-bottom: 1px solid #e5e7eb;
}

tr:hover {
    background: #f8fafc;
}

.badge {
    padding: 5px 10px;
    border-radius: 20px;
    background: #dbeafe;
    color: #1e40af;
    font-size: 13px;
}

.footer {
    text-align: center;
    padding: 25px;
    color: #64748b;
}

</style>

</head>

<body>

<div class="header">

<h1>Student Placement & Internship Management System</h1>

<p>DBMS + Node.js Localhost Application</p>

</div>

<div class="container">

<div class="cards">

<div class="card">
<h2>${data.students.length}</h2>
<p>Students</p>
</div>

<div class="card">
<h2>${data.companies.length}</h2>
<p>Companies</p>
</div>

<div class="card">
<h2>${data.drives.length}</h2>
<p>Job Drives</p>
</div>

<div class="card">
<h2>${data.applications.length}</h2>
<p>Applications</p>
</div>

</div>


<div class="section">

<h2>Students</h2>

<table>

<tr>
<th>Name</th>
<th>Roll No</th>
<th>Department</th>
<th>CGPA</th>
</tr>
`;

    data.students.forEach(student => {

        html += `
<tr>
<td>${student.name}</td>
<td>${student.roll_no}</td>
<td>${student.department_name}</td>
<td>${student.cgpa}</td>
</tr>
`;

    });

    html += `
</table>
</div>


<div class="section">

<h2>Companies</h2>

<table>

<tr>
<th>Company</th>
<th>Industry</th>
<th>Location</th>
</tr>
`;

    data.companies.forEach(company => {

        html += `
<tr>
<td>${company.company_name}</td>
<td>${company.industry}</td>
<td>${company.location}</td>
</tr>
`;

    });

    html += `
</table>
</div>


<div class="section">

<h2>Job Drives</h2>

<table>

<tr>
<th>Company</th>
<th>Role</th>
<th>Type</th>
<th>Minimum CGPA</th>
<th>Package</th>
<th>Drive Date</th>
<th>Vacancies</th>
</tr>
`;

    data.drives.forEach(drive => {

        html += `
<tr>
<td>${drive.company_name}</td>
<td>${drive.job_role}</td>
<td>${drive.job_type}</td>
<td>${drive.minimum_cgpa}</td>
<td>${drive.package_lpa} LPA</td>
<td>${drive.drive_date}</td>
<td>${drive.vacancies}</td>
</tr>
`;

    });

    html += `
</table>
</div>


<div class="section">

<h2>Applications</h2>

<table>

<tr>
<th>Student</th>
<th>Company</th>
<th>Role</th>
<th>Package</th>
<th>Application Date</th>
<th>Status</th>
</tr>
`;

    data.applications.forEach(application => {

        html += `
<tr>
<td>${application.student_name}</td>
<td>${application.company_name}</td>
<td>${application.job_role}</td>
<td>${application.package_lpa} LPA</td>
<td>${application.application_date}</td>
<td><span class="badge">${application.status}</span></td>
</tr>
`;

    });

    html += `
</table>
</div>


<div class="section">

<h2>Interview Rounds</h2>

<table>

<tr>
<th>Student</th>
<th>Company</th>
<th>Round</th>
<th>Date</th>
<th>Result</th>
<th>Remarks</th>
</tr>
`;

    data.interviews.forEach(interview => {

        html += `
<tr>
<td>${interview.student_name}</td>
<td>${interview.company_name}</td>
<td>${interview.round_name}</td>
<td>${interview.round_date}</td>
<td><span class="badge">${interview.result}</span></td>
<td>${interview.remarks}</td>
</tr>
`;

    });

    html += `
</table>
</div>


<div class="section">

<h2>Placement Results</h2>

<table>

<tr>
<th>Student</th>
<th>Company</th>
<th>Role</th>
<th>Status</th>
<th>Joining Date</th>
<th>Package</th>
</tr>
`;

    data.placements.forEach(placement => {

        html += `
<tr>
<td>${placement.student_name}</td>
<td>${placement.company_name}</td>
<td>${placement.job_role}</td>
<td><span class="badge">${placement.final_status}</span></td>
<td>${placement.joining_date}</td>
<td>${placement.offered_package} LPA</td>
</tr>
`;

    });

    html += `
</table>
</div>


<div class="section">

<h2>Internships</h2>

<table>

<tr>
<th>Student</th>
<th>Company</th>
<th>Role</th>
<th>Start Date</th>
<th>End Date</th>
<th>Stipend</th>
<th>Status</th>
</tr>
`;

    data.internships.forEach(internship => {

        html += `
<tr>
<td>${internship.student_name}</td>
<td>${internship.company_name}</td>
<td>${internship.internship_role}</td>
<td>${internship.start_date}</td>
<td>${internship.end_date}</td>
<td>₹${internship.stipend}</td>
<td><span class="badge">${internship.status}</span></td>
</tr>
`;

    });

    html += `
</table>
</div>

</div>

<div class="footer">

Student Placement & Internship Management System

</div>

</body>
</html>
`;

    res.send(html);
}


app.listen(PORT, () => {

    console.log(`Server running at http://localhost:${PORT}`);

});