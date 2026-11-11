# Publish this repository on GitHub

The project has a local Git history with incremental commits. To publish it as a public repository:

1. Create an empty **public** repository on GitHub named `Assignment6_SmartCodeInspection` (do not initialize it with a README).
2. Open a terminal in this project directory and run:

```bash
git remote add origin https://github.com/YOUR_USERNAME/Assignment6_SmartCodeInspection.git
git branch -M main
git push -u origin main
git push origin v1.0
```

Replace `YOUR_USERNAME` with your GitHub username. The Git history, including the separate `TechnicalDebtVisitor` feature commit, will be pushed with the branch.
