# GitHub Setup Instructions for Oak Donuts

## Step 1: Create a New Repository on GitHub

1. Go to [GitHub.com](https://github.com) and log in
2. Click the **"+"** icon in the top right corner
3. Select **"New repository"**
4. Fill in the details:
   - **Repository name**: `OakDonuts` (or your preferred name)
   - **Description**: `Java GUI Donut Shop Menu and Ordering System with Derby Database`
   - **Visibility**: Choose **Public** or **Private**
   - **Do NOT** initialize with README, .gitignore, or license (we already have these)
5. Click **"Create repository"**

## Step 2: Add Remote and Push to GitHub

After creating the repository on GitHub, you'll see commands like:

```bash
git remote add origin https://github.com/YOUR_USERNAME/OakDonuts.git
git branch -M main
git push -u origin main
```

Run these commands in the OakDonuts directory:

```bash
cd /Users/alexpozo/development/Donut-shop-menu/OakDonuts

# Add the remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/OakDonuts.git

# Rename branch to main
git branch -M main

# Push to GitHub
git push -u origin main
```

## Step 3: Verify on GitHub

1. Go to your GitHub repository URL
2. Verify that all files are visible:
   - Source code in `src/` directory
   - Screenshots in `capture/` directory
   - README.md file
   - .gitignore file

## Repository URL Format

Your final GitHub repository URL will be:
```
https://github.com/YOUR_USERNAME/OakDonuts
```

## Troubleshooting

### If you get authentication errors:
- Make sure you have GitHub CLI installed: `brew install gh`
- Or use SSH keys: `gh auth login`
- Or generate a Personal Access Token on GitHub settings

### If you need to change the remote:
```bash
git remote remove origin
git remote add origin https://github.com/YOUR_USERNAME/OakDonuts.git
git push -u origin main
```

## Project Contents

Your GitHub repository will contain:

```
├── README.md                          (Comprehensive documentation)
├── GITHUB_SETUP.md                   (This file)
├── .gitignore                        (Git ignore rules)
├── src/main/java/com/oakdonuts/
│   ├── models/
│   │   ├── MenuItem.java
│   │   └── Order.java
│   ├── database/
│   │   └── DatabaseManager.java
│   ├── controllers/
│   │   └── OrderController.java
│   └── views/
│       ├── MainFrame.java
│       ├── MenuPanel.java
│       ├── OrderPanel.java
│       └── OrderHistoryPanel.java
├── capture/
│   ├── screenshot_1_menu.png
│   ├── screenshot_2_place_order.png
│   └── screenshot_3_order_history.png
└── lib/
    ├── derby.jar
    ├── derbyclient.jar
    └── derbyshared.jar
```

## Next Steps

Once pushed to GitHub:
1. Share your repository URL: `https://github.com/YOUR_USERNAME/OakDonuts`
2. Share the local path where the project is stored
3. The `capture/` folder contains all screenshots

---

**Good luck! 🍩**
