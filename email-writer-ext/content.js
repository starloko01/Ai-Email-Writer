console.log("Email Writer Extension Loaded");

function getEmailContent() {
    const selectors = [
        '.h7',
        'a3s.aiL',
        '.gmail_quote',
        '[role = "presentation"]'
    ];
    for (const selector of selectors) {
        const content = document.querySelector(selector);
        if (content) return content.innerText.trim();
        return '';
    }
    
}

function findComposeToolbar(targetNode) {
  // Search for toolbar inside the compose/reply container
  const selectors = ['.btC', '.aDh', '[role="toolbar"]', '.gU.Up'];
  for (const selector of selectors) {
    const toolbar = targetNode.querySelector(selector);
    if (toolbar) return toolbar;
  }
  return null;
}

function createAIButton() {
  const button = document.createElement('div');
  button.className = 'T-I J-J5-Ji aoO v7 T-I-atl L3 ai-reply-button';
  button.style.marginRight = '8px';
  button.textContent = 'AI Reply';
  button.setAttribute('role', 'button');
  button.setAttribute('data-tooltip', 'Generate AI Reply');
  return button;
}

function injectButton(targetNode) {
  
  if (targetNode.querySelector('.ai-reply-button')) return;

  const toolbar = findComposeToolbar(targetNode);
  if (!toolbar) {
    console.log('Toolbar not found in this compose/reply window');
    return;
  }

  console.log('Toolbar found -> injecting AI Reply button');
  const button = createAIButton();

  button.addEventListener('click', async () => {
    try {
        button.innerHTML = 'Generating';
        button.disabled = true;
        const emailContent = getEmailContent();

        const response = await fetch('http://localhost:8080/api/email/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                emailContent: emailContent,
                tone: "professional"
            })
        });

        if(!response.ok){
            throw new Error("API Request Failed");
        }

        const generatedReply = await response.text();

        const composeBox = document.querySelector(
            '[role="textbox"][g_editable="true"]'
        );

        if(composeBox){
            composeBox.focus();
            document.execCommand('insertText', false, generatedReply);
        }

    } catch (error) {
        
    } finally {
        button.innerHTML = 'AI Reply';
        button.disabled = false
    }
  });
  toolbar.insertBefore(button, toolbar.firstChild);
}

const observer = new MutationObserver((mutations) => {
  for (const mutation of mutations) {
    for (const node of mutation.addedNodes) {
      if (node.nodeType !== Node.ELEMENT_NODE) continue;

      
      if (node.matches('[role="dialog"]') || node.querySelector?.('[role="dialog"]')) {
        console.log('Detected Compose window');
        setTimeout(() => injectButton(node), 800);
      }

      const hasReplyBody =
        node.querySelector?.('[aria-label="Message Body"]') ||
        node.querySelector?.('div[role="textbox"]');

      if (hasReplyBody) {
        const replyContainer =
          node.closest('div[aria-label="Reply"]') ||
          node.closest('.adn') ||
          node;

        if (replyContainer) {
          console.log('Detected Reply window');
          setTimeout(() => injectButton(replyContainer), 800);
        }
      }
    }
  }
});

// Start observing Gmail’s DOM for dynamic changes
observer.observe(document.body, { childList: true, subtree: true });
