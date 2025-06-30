@echo off
echo TaskScheduler: Preparing for GitHub push
echo =================================
echo.

REM Run cleanup script first
echo Running cleanup to remove unnecessary files...
call github-cleanup.bat

echo.
echo Adding files to Git...
git add .

echo.
set /p commit_msg="Enter commit message: "

echo.
echo Committing changes: "%commit_msg%"
git commit -m "%commit_msg%"

echo.
echo Pushing to GitHub...
git push

echo.
echo Push completed successfully!
echo.
pause
