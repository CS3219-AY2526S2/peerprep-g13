-- easy
INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Two Sum',
           'Arrays',
           'easy',
           'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. You may assume that each input would have exactly one solution, and you may not use the same element twice.',
           '["Input: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: nums[0] + nums[1] == 9, so return [0, 1].", "Input: nums = [3,2,4], target = 6\nOutput: [1,2]"]',
           '["2 <= nums.length <= 10^4", "-10^9 <= nums[i] <= 10^9", "-10^9 <= target <= 10^9", "Only one valid answer exists."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Reverse String',
           'Strings',
           'easy',
           'Write a function that reverses a string. The input string is given as an array of characters s. You must do this by modifying the input array in-place with O(1) extra memory.',
           '["Input: s = [\"h\",\"e\",\"l\",\"l\",\"o\"]\nOutput: [\"o\",\"l\",\"l\",\"e\",\"h\"]", "Input: s = [\"H\",\"a\",\"n\",\"n\",\"a\",\"h\"]\nOutput: [\"h\",\"a\",\"n\",\"n\",\"a\",\"H\"]"]',
           '["1 <= s.length <= 10^5", "s[i] is a printable ASCII character."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Valid Parentheses',
           'Stack',
           'easy',
           'Given a string s containing just the characters ''('', '')'', ''{'', ''}'', ''['' and '']'', determine if the input string is valid. An input string is valid if: open brackets must be closed by the same type of brackets, and open brackets must be closed in the correct order.',
           '["Input: s = \"()\"\nOutput: true", "Input: s = \"()[]{}\"\nOutput: true", "Input: s = \"(]\"\nOutput: false"]',
           '["1 <= s.length <= 10^4", "s consists of parentheses only ''()[]{}''."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Merge Two Sorted Lists',
           'Linked List',
           'easy',
           'You are given the heads of two sorted linked lists list1 and list2. Merge the two lists into one sorted list. The list should be made by splicing together the nodes of the first two lists. Return the head of the merged linked list.',
           '["Input: list1 = [1,2,4], list2 = [1,3,4]\nOutput: [1,1,2,3,4,4]", "Input: list1 = [], list2 = []\nOutput: []"]',
           '["The number of nodes in both lists is in the range [0, 50].", "-100 <= Node.val <= 100", "Both list1 and list2 are sorted in non-decreasing order."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Maximum Depth of Binary Tree',
           'Trees',
           'easy',
           'Given the root of a binary tree, return its maximum depth. A binary tree''s maximum depth is the number of nodes along the longest path from the root node down to the farthest leaf node.',
           '["Input: root = [3,9,20,null,null,15,7]\nOutput: 3", "Input: root = [1,null,2]\nOutput: 2"]',
           '["The number of nodes in the tree is in the range [0, 10^4].", "-100 <= Node.val <= 100"]',
           '[]',
           0, 0, true
       );

-- medium
INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Longest Substring Without Repeating Characters',
           'Strings',
           'medium',
           'Given a string s, find the length of the longest substring without repeating characters.',
           '["Input: s = \"abcabcbb\"\nOutput: 3\nExplanation: The answer is \"abc\", with the length of 3.", "Input: s = \"bbbbb\"\nOutput: 1", "Input: s = \"pwwkew\"\nOutput: 3"]',
           '["0 <= s.length <= 5 * 10^4", "s consists of English letters, digits, symbols and spaces."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           '3Sum',
           'Arrays',
           'medium',
           'Given an integer array nums, return all the triplets [nums[i], nums[j], nums[k]] such that i != j, i != k, j != k, and nums[i] + nums[j] + nums[k] == 0. Notice that the solution set must not contain duplicate triplets.',
           '["Input: nums = [-1,0,1,2,-1,-4]\nOutput: [[-1,-1,2],[-1,0,1]]", "Input: nums = [0,1,1]\nOutput: []", "Input: nums = [0,0,0]\nOutput: [[0,0,0]]"]',
           '["3 <= nums.length <= 3000", "-10^5 <= nums[i] <= 10^5"]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Binary Tree Level Order Traversal',
           'Trees',
           'medium',
           'Given the root of a binary tree, return the level order traversal of its nodes'' values (i.e., from left to right, level by level).',
           '["Input: root = [3,9,20,null,null,15,7]\nOutput: [[3],[9,20],[15,7]]", "Input: root = [1]\nOutput: [[1]]", "Input: root = []\nOutput: []"]',
           '["The number of nodes in the tree is in the range [0, 2000].", "-1000 <= Node.val <= 1000"]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Clone Graph',
           'Graphs',
           'medium',
           'Given a reference of a node in a connected undirected graph, return a deep copy (clone) of the graph. Each node in the graph contains a value (int) and a list (List[Node]) of its neighbors.',
           '["Input: adjList = [[2,4],[1,3],[2,4],[1,3]]\nOutput: [[2,4],[1,3],[2,4],[1,3]]", "Input: adjList = [[]]\nOutput: [[]]"]',
           '["The number of nodes in the graph is in the range [1, 100].", "1 <= Node.val <= 100", "Node.val is unique for each node.", "There are no repeated edges and no self-loops in the graph."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Coin Change',
           'Dynamic Programming',
           'medium',
           'You are given an integer array coins representing coins of different denominations and an integer amount representing a total amount of money. Return the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return -1.',
           '["Input: coins = [1,5,11], amount = 11\nOutput: 1", "Input: coins = [2], amount = 3\nOutput: -1", "Input: coins = [1,2,5], amount = 11\nOutput: 3\nExplanation: 11 = 5 + 5 + 1"]',
           '["1 <= coins.length <= 12", "1 <= coins[i] <= 2^31 - 1", "0 <= amount <= 10^4"]',
           '[]',
           0, 0, true
       );

--hard
INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Median of Two Sorted Arrays',
           'Arrays',
           'hard',
           'Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the two sorted arrays. The overall run time complexity should be O(log (m+n)).',
           '["Input: nums1 = [1,3], nums2 = [2]\nOutput: 2.00000\nExplanation: merged array = [1,2,3] and median is 2.", "Input: nums1 = [1,2], nums2 = [3,4]\nOutput: 2.50000\nExplanation: merged array = [1,2,3,4] and median is (2 + 3) / 2 = 2.5."]',
           '["nums1.length == m", "nums2.length == n", "0 <= m <= 1000", "0 <= n <= 1000", "1 <= m + n <= 2000", "-10^6 <= nums1[i], nums2[i] <= 10^6"]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Word Ladder',
           'Graphs',
           'hard',
           'A transformation sequence from word beginWord to word endWord using a dictionary wordList is a sequence beginWord -> s1 -> s2 -> ... -> sk such that every adjacent pair of words differs by a single letter. Given two words, beginWord and endWord, and a dictionary wordList, return the number of words in the shortest transformation sequence, or 0 if no such sequence exists.',
           '["Input: beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\",\"cog\"]\nOutput: 5\nExplanation: hit -> hot -> dot -> dog -> cog", "Input: beginWord = \"hit\", endWord = \"cog\", wordList = [\"hot\",\"dot\",\"dog\",\"lot\",\"log\"]\nOutput: 0"]',
           '["1 <= beginWord.length <= 10", "endWord.length == beginWord.length", "1 <= wordList.length <= 5000", "All words consist of lowercase English letters.", "All words in wordList are unique."]',
           '[]',
           0, 0, true
       );

INSERT INTO questions (title, topic, difficulty, prompt, example, constraints, image_urls, created_by, updated_by, is_active)
VALUES (
           'Serialize and Deserialize Binary Tree',
           'Trees',
           'hard',
           'Serialization is the process of converting a data structure or object into a sequence of bits so that it can be stored in a file or transmitted across a network. Design an algorithm to serialize and deserialize a binary tree. There is no restriction on how your serialization/deserialization algorithm should work.',
           '["Input: root = [1,2,3,null,null,4,5]\nOutput: [1,2,3,null,null,4,5]", "Input: root = []\nOutput: []"]',
           '["The number of nodes in the tree is in the range [0, 10^4].", "-1000 <= Node.val <= 1000"]',
           '[]',
           0, 0, true
       );